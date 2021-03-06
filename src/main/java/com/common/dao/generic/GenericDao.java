package com.common.dao.generic;

import com.common.bean.OrderByBean;
import com.common.dict.Constant2;
import com.common.util.ReflectHWUtils;
import com.common.util.SystemHWUtil;
import com.string.widget.util.ValueWidget;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/***
 * all dao must extends this class
 * 
 * @author huangwei
 * 
 * @param <T>
 */
public abstract class GenericDao<T> extends UniversalDao {
	protected final Class<T> clz = SystemHWUtil.getGenricClassType(getClass());
	/***
	 * generated by spring configuration file
	 */
    protected Logger logger = Logger.getLogger(this.getClass());

	
	/***************************************************************/

	/***
     * 获取实体类
     * @return
     */
    public Class<T> getEntityClass() {
        return clz;
    }

    /***
     * 创建空对象
     *
     * @return
     */
    public T createEmptyObj(){
        return createEmptyObj(clz);
    }
	/***
	 * 仅仅创建一个空对象而已,不涉及数据库操作
	 * @return
	 */
	public T createEmptyObj(Class<T> clz){
		/*if(ValueWidget.isNullOrEmpty(clz)){
			clz=SystemHWUtil.getGenricClassType(getClass());
		}*/
		try {
			return  clz.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
            logger.error("createEmptyObj error", e);
        } catch (IllegalAccessException e) {
			e.printStackTrace();
            logger.error("createEmptyObj error", e);
        }
		return null;
	}
	public void deleteById(int id) {
        if (softwareDelete(id, getClz())) return;
        this.getCurrentSession().delete(get(id));
        logger.debug("delete by id=\t" + id);
	}

	public void deleteById(long id) {
        if (softwareDelete(id, getClz())) return;
        this.getCurrentSession().delete(get(id));
        logger.debug("delete by id=\t" + id);
	}
	public void delete(List<Integer>ids) {
		int sum=ids.size();
		for(int i=0;i<sum;i++){
			int id=(Integer)ids.get(i);
			deleteById(id);
		}
	}
	public void deleteLong(List<Long>ids) {
		int sum=ids.size();
		for(int i=0;i<sum;i++){
			long id=(Long)ids.get(i);
			deleteById(id);
		}
	}
	public void updateSpecail(int id,String propertyName,String value){
		 updateSpecail(clz, id, propertyName, value);
	}

	public void updateSpecail(int id, String propertyName, int value) {
		updateSpecail(clz, id, propertyName, value);
	}

	public void updateSpecail(int id,String propertyName,String value,String propertyName2,String value2){
		updateSpecail(clz, id, propertyName, value, propertyName2, value2);
	}

	public void updateSpecail(int id, String propertyName, String value, String propertyName2, int value2) {
		updateSpecail(clz, id, propertyName, value, propertyName2, value2);
	}
	public int deleteByIdSimplely(int id) {
		return deleteByIdSimplely(clz, id);
	}
	
	public T get(int id) {
		return (T) this.getCurrentSession().get(clz, id);
	}
	/***
	 * 动态 eager抓取
	 * @param id
	 * @param child
	 * @return
	 */
	public T get(int id,String child){
		if(ValueWidget.isNullOrEmpty(child)){
			return get(id);
		}
		return (T) this.getCurrentSession().createCriteria(clz)
				.setFetchMode(child, FetchMode.EAGER).add(Restrictions.idEq(id)).uniqueResult();
	}
	/*public T get(int id,String child,Object childObj){//TODO
	 *子set是lazy,要动态抓取,并且可以设置子set的条件 
		if(ValueWidget.isNullOrEmpty(child)){
			return get(id);
		}
		Example example=Example.create( childObj).excludeZeroes();
		if(isLike){
			example.enableLike(MatchMode.ANYWHERE);
		}
		return (T) this.getCurrentSession().createCriteria(clz).setFetchMode("PaperNews.newsCommentSet", FetchMode.EAGER)
				.add(Restrictions.idEq(id))
				.createCriteria(child).add(example).uniqueResult();
	}*/
	
	public T get(long id) {
		return (T) this.getCurrentSession().get(clz, id);
	}

	public T get(String propertyName,Object propertyValue){
		return (T)this.getCurrentSession().createCriteria(clz).add(Restrictions.eq(propertyName, propertyValue)).uniqueResult();
	}
	public T get(String propertyName,Object propertyValue,String propertyName2,Object propertyValue2){
		return (T)this.getCurrentSession().createCriteria(clz)
				.add(Restrictions.eq(propertyName, propertyValue))
				.add(Restrictions.eq(propertyName2, propertyValue2))
				.uniqueResult();
	}

    /***
     * @param propertyName
     * @param propertyValue
     * @param propertyName2
     * @param propertyValue2Arr : in (1,2,3)
     * @return
     */
    public List getList(String propertyName, Object propertyValue, String propertyName2, Object[] propertyValue2Arr) {
        Criteria criteria = this.getCurrentSession().createCriteria(clz)
                .add(Restrictions.eq(propertyName, propertyValue));
        if (!ValueWidget.isNullOrEmpty(propertyName2) && !ValueWidget.isNullOrEmpty(propertyValue2Arr)) {
            criteria.add(Restrictions.in(propertyName2, propertyValue2Arr));
        }
        return criteria.list();
    }

    public T get(Map condition){
        Criteria criteria = this.getCurrentSession()
                .createCriteria(clz);
		criteria =condition(criteria, condition);
		return (T) criteria.uniqueResult();
	}

	/***
	 *
	 * @param obj  : obj的成员变量就是列名,值就是列的值<br>
	 * 过滤掉id
	 * @return
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
    public T getByBean(Object obj, String[] excludeProperties) throws SecurityException {
        Criteria criteria=this.getCurrentSession().createCriteria(clz);
		List<Field> fieldsList = ReflectHWUtils.getAllFieldList(obj.getClass());
		for(int i=0;i<fieldsList.size();i++){
			Field f=fieldsList.get(i);
            if (f.getName().equals(Constant2.DB_ID)) {//过滤掉id
                continue;
			}
            //!ValueWidget.isNullOrEmpty(excludeProperty)&&f.getName().equals(excludeProperty)
            if (SystemHWUtil.isContains(excludeProperties, f.getName())) {
                continue;
			}
			Object propertyValue=ReflectHWUtils.getObjectValue(obj, f);
			if(!ValueWidget.isNullOrEmpty(propertyValue)){
				criteria.add(Restrictions.eq(f.getName(), propertyValue));
			}
		}
		return (T)criteria.uniqueResult();
	}

    public T getByBean(Object obj, String excludeProperty) throws SecurityException {
        String[] excludeProperties = new String[]{excludeProperty};
        return getByBean(obj, excludeProperties);
    }

    public T getByBean(Object obj) throws SecurityException {
        return getByBean(obj,(String)null);
	}
	/***
	 * 设置FetchMode.LAZY
	 * 
	 * @param id
	 * @param propertyName
	 * @return
	 */
	public T getLazy(int id, String propertyName) {
        return (T) this.getCurrentSession().createCriteria(clz)
                .setFetchMode(propertyName, FetchMode.LAZY)
                .add(Restrictions.eq(Constant2.DB_ID, id)).uniqueResult();
    }

	/***
	 * 
	 * @param notNullColumn
	 * @param orderByMode : 排序方式:asc,desc
	 * @param orderedColumn  :  是哪个列排序
	 * @return
	 */
	public List<T> getAll(String notNullColumn,String orderByMode,String orderedColumn) {
        Criteria criteria = this.getCurrentSession()
                .createCriteria(clz);
		criteria=notNullColumn(notNullColumn, criteria ) ;
		orderBy(orderedColumn, orderByMode, criteria);
		return (List<T>)criteria.list();
	}
	public List<T> getAll(String notNullColumn,String orderByMode,String orderedColumn,String orderByMode2,String orderedColumn2) {
		Criteria criteria=this.getCurrentSession()
				.createCriteria(clz);
		notNullColumn(notNullColumn, criteria ) ;
		orderBy(orderedColumn, orderByMode, criteria);
		orderBy(orderedColumn2, orderByMode2, criteria);
		return (List<T>)criteria.list();
	}
	public List<T> getAll() {
		return getAll(null,null,null);
	}
	private Criteria notNullColumn(String notNullColumn,Criteria criteria){
		return notNullColumn(getClz(),notNullColumn, criteria);
	}
	/***
	 * 排除条件之后,再查询所有的
	 * @param exceptProperty
	 * @param propertyValue
	 * @return
	 */
	public List<T> getListExcept(String exceptProperty,Object propertyValue){
		if(ValueWidget.isNullOrEmpty(exceptProperty)){
			return (List<T>)getCurrentSession().createCriteria(clz).list();
		}
		return (List<T>)getCurrentSession().createCriteria(clz).add(Restrictions.ne(exceptProperty, propertyValue)).list();
	}
	public List<T> getList(String property,Object propertyValue){
        return getList(property, propertyValue, true);
    }

    public List<T> getList(String property, Object propertyValue, boolean isLike) {
        Criteria criteria =getCurrentSession().createCriteria(clz);
		if(ValueWidget.isNullOrEmpty(property)){
			return (List<T>)criteria.list();
		}
        Criterion criterion = null;
        if (isLike) {
            criterion = Restrictions.like(property, "%" + propertyValue + "%");
        } else {
            criterion = Restrictions.eq(property, propertyValue);
        }
        return (List<T>) criteria.add(criterion).list();
    }
	
	/***
	 * 
	 * @param property : 属性名
	 * @param propertyValue : 属性值
	 * @param orderByMode
	 * @param orderedColumn
	 * @param orderByMode2
	 * @param orderedColumn2
	 * @return
	 */
	public List<T> find(String property,Object propertyValue,String orderByMode,String orderedColumn,String orderByMode2,String orderedColumn2){
		Map condition=new HashMap();
		condition.put(property, propertyValue);
		return find(condition, orderByMode, orderedColumn, orderByMode2, orderedColumn2);
	}
	public List<T> find(Map condition,String orderByMode,String orderedColumn,String orderByMode2,String orderedColumn2){
		List<OrderByBean> list=null;
		if(!(ValueWidget.isNullOrEmpty(orderByMode) 
				&& ValueWidget.isNullOrEmpty(orderedColumn) 
				&& ValueWidget.isNullOrEmpty(orderByMode2) 
				&&ValueWidget.isNullOrEmpty(orderedColumn2) )){
			list=orderByBeans(orderByMode, orderedColumn, orderByMode2, orderedColumn2);
		}
		return find(condition, list);
	}
	/***
	 * 
	 * @param condition
	 * @param orderByBeans
	 * @return
	 */
	public List<T> find(Map condition,List<OrderByBean> orderByBeans) {
		Criteria criteria = getCurrentSession().createCriteria(clz);
		condition(criteria, condition);
		if(!ValueWidget.isNullOrEmpty(orderByBeans)){
			for(int i=0;i<orderByBeans.size();i++){
				OrderByBean byBean=orderByBeans.get(i);
				orderBy(byBean.getOrderedColumn(), byBean.getOrderMode(), criteria);
			}
		}
		return criteria.list();
	}
	/***
	 * Find in DB depending on conditions.
	 * 
	 * @param obj
	 * @param includeZeros<br />
	 *            : Whether to include query criteria which field is 0. true:add
	 *            [where xxx=0]; false:no [where xxx=0]
	 * @param orderBy : 排序的column
	 * @return list
	 */
	public List<T> find(Object obj, boolean includeZeros,String orderBy,String orderMode,boolean isDISTINCT_ROOT_ENTITY,boolean isLike) {
		if (obj == null) {
			if(isDISTINCT_ROOT_ENTITY){
				return (List<T>) this.getCurrentSession()
				.createCriteria(clz).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
			}else{
				return this.getAll();
			}
		} else {
			Criteria criteria = getCriteria(clz,null/*Map*/, obj, includeZeros,isLike);
			orderBy(orderBy, orderMode, criteria);
			if(isDISTINCT_ROOT_ENTITY){
				criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
			}
			List<T> list=null;
			try {
				list = (List<T>) criteria.list();
			} catch (org.hibernate.ObjectNotFoundException e) {
				e.printStackTrace();
			}
			return list;
		}
	}

	public List<T> find(T obj, boolean includeZeros,boolean isLike) {
		return find(obj, includeZeros, null, null,false,isLike);
	}
	/***
	 * 分页查询<br />
	 * obj 和includeZeros 是配套的
	 * @param obj : 查询条件<br />Example.create(obj)
	 * @param includeZeros
	 *            : Whether to include query criteria which field is 0. true:add
	 *            [where xxx=0]; false:no [where xxx=0]
	 * @param first
	 * @param maxRecordsNum
	 * @return
	 */
	public List<T> find(Map condition, Object obj, boolean includeZeros,
			int first, int maxRecordsNum,boolean isDistinctRoot,boolean isLike) {
		if (obj == null) {
			return this.getCriteriaByPage(clz,condition, first, maxRecordsNum,isDistinctRoot).list();
		} else {
			Criteria criteria = getCriteria(clz,condition, obj, includeZeros,
					first, maxRecordsNum,isDistinctRoot,isLike);
			return (List<T>) criteria.list();
		}
	}
	public List<T> find(String key,Object value){
		Map condition=new HashMap();
		condition.put(key, value);
		return find(condition);
	}
	public List<T> find(Map condition, Object obj, boolean includeZeros,
			int first, int maxRecordsNum ,boolean isLike) {
		return find(condition, obj, includeZeros, first, maxRecordsNum, false, isLike);
	}
	//TODO
	/*public List<T> find(T obj, boolean includeZeros,
			int first, int maxRecordsNum,boolean isLike) {
		if (obj == null) {
			return this.getCriteriaByPage(clz,obj,includeZeros,first, maxRecordsNum).list();
		} else {
			Criteria criteria = getCriteria(obj, includeZeros,
					first, maxRecordsNum,isLike);
			return (List<T>) criteria.list();
		}
	}*/

	protected Criteria getCriteria(Object obj, boolean includeZeros,boolean isLike,boolean isDistinctRoot,String notNullColumn) {
		return getCriteria(clz, obj, includeZeros, isLike, isDistinctRoot, notNullColumn);
	}
	
	private Criteria getCriteria(Object obj,
								 boolean includeZeros, int first, int maxRecordsNum, boolean isLike) {
		return getCriteria(clz, obj, includeZeros, first, maxRecordsNum,isLike);
	}
	
	
	/***
	 * paging，并且可以添加查询条件
	 * 
	 * @param condition
	 * @param first
	 *            : if value is "-1", indicate no paging
	 * @param maxRecordsNum
	 * @return
	 */
	public List<T> listByPage(Map condition, int first, int maxRecordsNum,boolean isDistinctRoot) {
		Criteria criteria = getCriteriaByPage(clz,condition, first, maxRecordsNum,isDistinctRoot);
		return criteria.list();
	}
	public long count(Map condition,boolean isDistinctRoot) {
		return count(clz, condition, isDistinctRoot);
	}
	public long count(Map condition ) {
		return count(clz, condition);
	}
	public long count(T obj, boolean includeZeros,boolean isLike) {
		return count(clz, obj, includeZeros, isLike);
	}
	
	
	/***
	 * 获取前n个记录
	 * @param condition
	 * @param front
	 * @return
	 */
	public List<T> getFrontRecords(Map condition,int front){
		return listByPage(condition, 0, front, true);
	}
	public long listByPage(Map condition, List list, int first,
			int maxRecordsNum,boolean isDistinctRoot) {
		return listByPage(clz, condition, list, first, maxRecordsNum, isDistinctRoot);
	}
	/***
	 * 
	 * @param key : 条件查询的列名
	 * @param value
	 * @param list
	 * @param first
	 * @param maxRecordsNum
	 * @return
	 */
	public long listByPage(Class clz,String key,Object value, List<T> list, int first,
			int maxRecordsNum) {
		listIsNull(list);

		Criteria criteria = getCriteriaByPage(clz,key,value, first, maxRecordsNum);

		list.addAll(criteria.list());/* 获取查询结果 */
		return count(clz,key,value);
	}



	public long listByPage(Object conditonObj,boolean includeZeros,boolean isLike, List<T> list, int first,
			int maxRecordsNum,boolean isDistinctRoot,String orderMode,String orderColumn,String notNullColumn){
		return listByPage(conditonObj, includeZeros, isLike, list, first, maxRecordsNum, isDistinctRoot, orderMode, orderColumn, notNullColumn, null, null);
	}
	
	public long listByPage(Object conditonObj,boolean includeZeros,boolean isLike, List list, int first,
			int maxRecordsNum,boolean isDistinctRoot,String orderMode,String orderColumn,String notNullColumn,String orderMode2,String orderColumn2) {
		return listByPage(clz, conditonObj, includeZeros, isLike, list, first, maxRecordsNum, isDistinctRoot, orderMode, orderColumn, notNullColumn, orderMode2, orderColumn2);
	}
	
	public long listByPage(Object conditonObj,boolean includeZeros,boolean isLike, List list, int first,
			int maxRecordsNum,boolean isDistinctRoot,String notNullColumn,ListOrderedMap orderColumnModeMap) {
		return listByPage(clz, conditonObj, includeZeros, isLike, list, first, maxRecordsNum, isDistinctRoot, notNullColumn, orderColumnModeMap);
	}
	/***
	 * 
	 * @param conditonObj : Map or entity object
	 * @param isLike
	 * @param first
	 * @param maxRecordsNum
	 * @param notNullColumn
	 * @param orderColumnModeMap
	 * @return
	 */
	public List<T> find(Object conditonObj,boolean isLike, int first,
			int maxRecordsNum,String notNullColumn,ListOrderedMap orderColumnModeMap)
	{
		return find(conditonObj, false, isLike, first, maxRecordsNum, true, notNullColumn, orderColumnModeMap);
	}

	/***
     * @param conditonObj : 可以是map ,也可以是实体类
     * @param maxRecordsNum
	 * @param notNullColumn
	 * @param orderColumnModeMap
     * @return
     */
    public List<T> getFrontList(Object conditonObj, int maxRecordsNum, String notNullColumn, ListOrderedMap orderColumnModeMap) {
        return find(conditonObj, false, 0, maxRecordsNum, notNullColumn, orderColumnModeMap);
    }

    /***
     * @param maxRecordsNum
     * @param notNullColumn
     * @param orderColumnModeMap
     * @return
     */
    public List<T> getFrontList(int maxRecordsNum, String notNullColumn, ListOrderedMap orderColumnModeMap) {
        return getFrontList(null, maxRecordsNum, notNullColumn, orderColumnModeMap);
    }

    /***
	 * 
	 * @param conditonObj : Map or entity object
	 * @param includeZeros : Whether to include query criteria which field is 0.
	 *            true:add[where xxx=0]; false:no [where xxx=0]
	 * @param isLike
	 * @param first
	 * @param maxRecordsNum
	 * @param isDistinctRoot
	 * @param notNullColumn
	 * @param orderColumnModeMap
	 * @return
	 */
	public List<T> find(Object conditonObj,boolean includeZeros,boolean isLike, int first,
			int maxRecordsNum,boolean isDistinctRoot,String notNullColumn,ListOrderedMap orderColumnModeMap) {
		Criteria criteria2=getCriteria( conditonObj, includeZeros,isLike,isDistinctRoot, notNullColumn);
		orderBy(orderColumnModeMap, criteria2);
		paging(criteria2, first, maxRecordsNum);
		return criteria2.list();/* 获取查询结果 */
	}



	public long listByPage(Map condition, String[]columns, String keyword, List list, int first,
                           int maxRecordsNum, String orderMode, String orderColumn, String orderMode2, String orderColumn2, boolean isAccurate) {
        return listByPage(clz, condition, columns, keyword, list, first, maxRecordsNum, orderMode, orderColumn2, orderMode2, orderColumn2, isAccurate);
    }
	public long listByPage(Map condition, String[]columns, String keyword, List list, int first,
                           int maxRecordsNum, ListOrderedMap orderColumnModeMap, boolean isAccurate) {
        return listByPage(clz, condition, columns, keyword, list, first, maxRecordsNum, orderColumnModeMap, isAccurate);
    }

    /*** select
     count(*) as y0_
     from
     t_test_to_boy this_
     where
     this_.status=?
     and (
     (
     this_.testcase like ?
     or this_.alias like ?
     )
     or this_.alias2 like ?
     )<br >
     * search
     * @param condition
     * @param columns
     * @param keyword
     * @return
     */
    protected Criteria getCriteria(Map condition,String[]columns,String keyword){
        return getCriteria(clz, condition, columns, keyword, false);
    }
	
	/***
	 * 
	 * @param condition : 查询条件
	 * @param first : 值为-1 则忽略
	 * @param maxRecordsNum : 值为-1 则忽略
	 * @return
	 */
	public List<T> find(Map condition, 
			int first, int maxRecordsNum) {
        Criteria criteria = this.getCurrentSession()
                .createCriteria(clz);

		condition(criteria, condition);
		paging(criteria, first, maxRecordsNum);
		
		return criteria.list();
	}

	/***
	 * 不分页，是方法listByPage(Map, int, int)的重载.
	 * 
	 * @param condition
	 * @return
	 */
	public List<T> listByPage(Map condition,boolean isDistinctRoot) {
		return listByPage(condition, SystemHWUtil.NEGATIVE_ONE,
				SystemHWUtil.NEGATIVE_ONE,isDistinctRoot);
	}
	public List<T> listByPage(Map condition ) {
		return listByPage(condition, false/*isDistinctRoot*/);
	}
	/***
	 * 通过条件来查询
	 * @param condition : 查询条件
	 * @return
	 */
	public List<T> find(Map condition) {
		return listByPage(condition);
	}
	public Class<T> getClz() {
		return clz;
	}

	public T load(int id) {
		logger.debug("load:id=" + id);
		return (T) this.getCurrentSession().load(clz, id);
	}

	public T load(long id) {
		logger.debug("load:id=" + id);
		return (T) this.getCurrentSession().load(clz, id);
	}
	public Object getOnePropertyById2(int id,String propertyName){
		return getOnePropertyById2(clz, id, propertyName);
	}
    
    public String getStringById(int id,String propertyName){
    	return getStringById(clz.getCanonicalName(), id, propertyName);
    }
    
    public Object[] getPropertiesById2(int id,String propertyName1,String propertyName2){
    	return getPropertiesById2(clz, id, propertyName1, propertyName2);
    }
    public Object[] getPropertiesById(int id,String[] propertyNames){
    	return getPropertiesById(clz.getCanonicalName(), id, propertyNames);
    }
    
    public Object[] getPropertiesById2(int id,String[] propertyNames){
    	return getPropertiesById2(clz, id, propertyNames);
    }
    public long count(String key,Object value) {
    	return count(clz,key, value);
    }
    protected Criteria getCriteria(Map condition, Object obj, boolean includeZeros,boolean isLike){
    	return getCriteria(clz,condition, obj, includeZeros,isLike);
    }
    public Criteria getCriteriaByPage(Map condition, int first,
			int maxRecordsNum,boolean isDistinctRoot) {
    	return getCriteriaByPage(clz, condition, first, maxRecordsNum, isDistinctRoot);
    }
}
