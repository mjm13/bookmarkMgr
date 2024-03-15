package com.bookmark.analysis.services;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.util.StrUtil;
import com.bookmark.analysis.common.util.SSLHelper;
import com.bookmark.analysis.dao.BaseDao;
import com.bookmark.analysis.dao.WebsiteDao;
import com.bookmark.analysis.dto.WebsiteQueryDto;
import com.bookmark.analysis.entity.Website;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.bookmark.analysis.common.util.SSLHelper.USER_AGENT;

/**
 * @author mjm
 * @createtime 2019/11/8-10:28
 **/
@Service
@Slf4j
public class WebsiteService extends BaseService<Website, Long> {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private WebsiteDao websiteDao;

    @Override
    public BaseDao<Website, Long> getDao() {
        return websiteDao;
    }

//    public void createIndexer() {
//        try {
//            FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
//            fullTextEntityManager.createIndexer().startAndWait();
//        } catch (InterruptedException e) {
//            log.error(e.getMessage(), e);
//        }
//    }

//    public List<Website> search(String text) {
//        // get the full text entity manager
//        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
//        // create the query using Hibernate Search query DSL
//        QueryBuilder queryBuilder = fullTextEntityManager.getSearchFactory().buildQueryBuilder().forEntity(Website.class).get();
//        // a very basic query by keywords
//        org.apache.lucene.search.Query query =
//                queryBuilder
//                        .keyword()
//                        .onFields("description", "keywords", "remark", "title", "url")
//                        .matching(text)
//                        .createQuery();
//        // wrap Lucene query in an Hibernate Query object
//        org.hibernate.search.jpa.FullTextQuery jpaQuery = fullTextEntityManager.createFullTextQuery(query, Website.class);
//        jpaQuery.setMaxResults(100);
//
//        List<Website> results = jpaQuery.getResultList();
//        return results;
//    }


    public Page<Website> findByPage(WebsiteQueryDto param) {
        Page<Website> websites = findAll((root, criteriaQuery, criteriaBuilder) -> {
            String keyword = param.getKeyword();
            String remark = param.getRemark();
            String title = param.getTitle();
            String url = param.getUrl();
            String description = param.getDescription();
            String domain = param.getDomain();
            String keywords = param.getKeywords();
            String loadResult = param.getLoadResult();

            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.isNotBlank(remark)) {
                predicates.add(criteriaBuilder.like(root.get("remark"), "%" + remark + "%"));
            }
            if (StringUtils.isNotBlank(title)) {
                predicates.add(criteriaBuilder.like(root.get("title"), "%" + title + "%"));
            }
            if (StringUtils.isNotBlank(url)) {
                predicates.add(criteriaBuilder.like(root.get("url"), "%" + url + "%"));
            }
            if (StringUtils.isNotBlank(description)) {
                predicates.add(criteriaBuilder.like(root.get("description"), "%" + description + "%"));
            }
            if ("is null".equals(domain)) {
                predicates.add(criteriaBuilder.isNull(root.get("domain")));
            } else if (StringUtils.isNotBlank(domain)) {
                predicates.add(criteriaBuilder.like(root.get("domain"), "%" + domain + "%"));
            }
            if (StringUtils.isNotBlank(remark)) {
                predicates.add(criteriaBuilder.like(root.get("keywords"), "%" + keywords + "%"));
            }
            if (StringUtils.isNotBlank(loadResult)) {
                predicates.add(criteriaBuilder.like(root.get("loadResult"), "%" + loadResult + "%"));
            }

            Predicate condition = criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            if (StringUtils.isNotBlank(keyword)) {
                keyword = "%" + keyword.toLowerCase() + "%";
                List<Predicate> orpres = genLikePredicates(root, criteriaBuilder, keyword, "title", "remark", "description", "keywords", "domain", "domainTitle", "url");
                Predicate key = criteriaBuilder.or(orpres.toArray(new Predicate[orpres.size()]));
                condition = criteriaBuilder.and(condition, key);
            }
            return condition;
        }, PageRequest.of(param.getPage() - 1, param.getLimit(), Sort.by("pageDate").descending()));
        return websites;
    }


    private List<Predicate> genLikePredicates(Root root, CriteriaBuilder criteriaBuilder, String keyword, String... props) {
        List<Predicate> orpres = new ArrayList<>();
        for (String prop : props) {
            orpres.add(criteriaBuilder.like(criteriaBuilder.lower(root.get(prop)), keyword));
        }
        return orpres;
    }

    @Async
    public void analysisWebsites(WebsiteQueryDto param) {
        SSLHelper.init();
        param.setLimit(100000);
        param.setPage(1);
        param.setLoadResult("访问网址异常");
        List<Website> datas = findByPage(param).getContent().stream().parallel().peek(website -> {
            String url = website.getUrl();
            if (!url.startsWith("http") || StrUtil.isNotBlank(website.getIcon())) {
                return;
            }
            try {
                Connection connect = Jsoup.connect(url).userAgent(USER_AGENT);
                connect.timeout(3000);
                connect.proxy("127.0.0.1", 49776);
                connect.ignoreHttpErrors(true);
                if (url.startsWith("https")) {
                    connect.followRedirects(true);
                    connect.timeout(60000);
                }
                Document doc = connect.get();

                Connection.Response response = connect.response();
                Elements body = doc.getElementsByTag("body");
                String title = doc.title();
                String keywords = doc.head().select("meta[name=keywords]").attr("content");
                String description = doc.head().select("meta[name=description]").attr("content");
                String icon = doc.head().select("link[type=image/x-icon]").attr("href");
                String icon2 = doc.head().select("link[rel='shortcut icon']").attr("href");
                String icon3 = doc.head().select("link[rel='fluid-icon']").attr("href");
                icon = StringUtils.defaultIfBlank(StringUtils.defaultIfBlank(icon, icon2), icon3);
                if (!icon.startsWith("http")) {
                    java.net.URL url1 = new URL(url);
                    icon = "http://" + url1.getHost() + "/" + icon;
                }

                String title2 = body.select("title").text();
                title = StringUtils.defaultIfBlank(title, title2);
                website.setKeywords(keywords);
                website.setIcon(icon);
                website.setDescription(description);
                website.setTitle(StringUtils.defaultIfBlank(title, "空白"));

                URL baseUrl = new URL(website.getUrl());
                Document baseDoc = Jsoup.connect(baseUrl.getProtocol() + "://" + baseUrl.getHost()).get();
                String baseTitle = baseDoc.head().select("title").text();
                website.setDomain(baseUrl.getHost());
                website.setDomainTitle(baseTitle);

                String pageDateStr = response.headers().get("Date");
                if (StringUtils.isNotBlank(pageDateStr)) {
                    Date pageDate = DatePattern.HTTP_DATETIME_FORMAT.parse(pageDateStr);
                    website.setPageDate(pageDate);
                }
                website.setLoadResult("采集结束");
                log.info("over url:{}", url);
            } catch (Exception e) {
                website.setTitle(e.getMessage());
                website.setLoadResult("访问网址异常");
                log.error(e.getMessage() + "url:" + url);
            }
        }).collect(Collectors.toList());

        websiteDao.saveAll(datas);
        log.info("分析书签结束!");
    }

}
