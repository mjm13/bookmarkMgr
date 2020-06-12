package com.bookmark.analysis.services;

import com.bookmark.analysis.common.bean.ExPage;
import com.bookmark.analysis.common.util.ExBeanUtil;
import com.bookmark.analysis.dao.BaseDao;
import com.bookmark.analysis.entity.BaseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * @author mjm
 * @createtime 2018/7/3 12:09
 */
@Transactional(rollbackFor = Exception.class)
public abstract class BaseService<T extends BaseEntity, ID extends Serializable> {

	private Integer exportExcelMaxData = 10000;

	@Autowired
	private JpaContext jpaContext;

	public void flush() {
		this.getDao().flush();
	}

	public EntityManager getEm() {
		return jpaContext.getEntityManagerByManagedType(BaseEntity.class);
	}

	/**
	 * 获得当前类DAO
	 *
	 * @return com.bmw.frame.dao.BaseDao<T, ID>
	 * @date 2018/7/3 10:55
	 **/
	public abstract BaseDao<T, ID> getDao();

	public Optional<T> findById(ID id) {
		return this.getDao().findById(id);
	}

	public List<T> findAllById(Iterable<ID> ids) {
		return this.getDao().findAllById(ids);
	}


	public List<T> findAll() {
		return this.getDao().findAll();
	}

	public List<T> findList(Specification<T> spec, Sort sort) {
		return this.getDao().findAll(spec, sort);
	}

	public Page<T> findAll(Pageable pageable) {
		return this.getDao().findAll(pageable);
	}

	public Page<T> findAll(ExPage exPage) {
		Pageable pageable = PageRequest.of(exPage.getPage() - 1, exPage.getLimit());
		return this.getDao().findAll(pageable);
	}

	public Page<T> findAll(ExPage exPage, Specification<T> specification) {
		Pageable pageable = PageRequest.of(exPage.getPage() - 1, exPage.getLimit());
		return this.getDao().findAll(specification, pageable);
	}

	public Page<T> findAll(Specification<T> specification, Pageable pageable) {
		return this.getDao().findAll(specification, pageable);
	}

	public Page<T> findAll(ExPage exPage, Specification<T> specification, Sort sort) {
		Pageable pageable = PageRequest.of(exPage.getPage() - 1, exPage.getLimit(), sort);
		return this.getDao().findAll(specification, pageable);
	}

	public List<T> findAll(Specification<T> specification, Sort sort) {
		return this.getDao().findAll(specification, sort);
	}

	public List<T> findAll(Specification<T> specification) {
		return this.getDao().findAll(specification);
	}


	public long count() {
		return this.getDao().count();
	}


	public long count(Specification<T> spec) {
		return this.getDao().count(spec);
	}


	public boolean exists(ID id) {
		return this.getDao().existsById(id);
	}


	public T save(T entity) {
		return this.getDao().save(entity);
	}

	public void saveAll(Iterable<T> entitys) {
		this.getDao().saveAll(entitys);
	}

	public T update(T entity) {
		return this.getDao().saveAndFlush(entity);
	}


	public void delete(ID id) {
		this.getDao().deleteById(id);
	}

	public void deleteAll() {
		this.getDao().deleteAll();
	}


	public void deleteByIds(@SuppressWarnings("unchecked") ID... ids) {
		if (ids != null) {
			for (int i = 0; i < ids.length; i++) {
				ID id = ids[i];
				this.delete(id);
			}
		}
	}


	public void delete(Iterable<T> entitys) {
		this.getDao().deleteAll(entitys);
	}

	public void delete(T entity) {
		this.getDao().delete(entity);
	}

	public Optional<T> findOne(Example<T> example) {
		return this.getDao().findOne(example);
	}

	public List<T> findAll(Example<T> example, Sort sort) {
		return this.getDao().findAll(example, sort);
	}

	public List<T> findAll(Example<T> example) {
		return this.getDao().findAll(example);
	}

	public long count(Example<T> example) {
		return this.getDao().count(example);
	}

	public boolean exists(Example<T> example) {
		return this.getDao().exists(example);
	}

	public ExPage getExportExPage(ExPage exPage) {
		exPage.setPage(1);
		exPage.setLimit(exportExcelMaxData);
		return exPage;
	}

	public Pageable getPageableByEx(ExPage exPage) {
		return PageRequest.of(exPage.getPage() - 1, exPage.getLimit());
	}

	public Pageable getPageableByEx(ExPage exPage, Sort sort) {
		return PageRequest.of(exPage.getPage() - 1, exPage.getLimit(), sort);
	}
}
