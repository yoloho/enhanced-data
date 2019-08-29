package com.yoloho.enhanced.data.dao.api.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.yoloho.enhanced.common.util.StringUtil;
import com.yoloho.enhanced.data.dao.api.ExprEntry;
import com.yoloho.enhanced.data.dao.api.ParamUtil;
import com.yoloho.enhanced.data.dao.api.filter.FieldCommand.Operator;
import com.yoloho.enhanced.data.dao.api.filter.FieldCommand.Type;
import com.yoloho.enhanced.data.dao.util.ColumnUtil;

/**
 * 查询过滤器: 封装前端提交的查询请求
 * 
 * ×将between...and分解为gt和lt
 * 
 */
public class DynamicQueryFilter implements java.io.Serializable {
	private static final long serialVersionUID = 8790528179232633456L;
	public static final Logger logger = LoggerFactory.getLogger(DynamicQueryFilter.class);

	private int offset = 0;
	private int limit = 20;
	private String filterName = "";
	private List<Object> paramValues;
	private List<QueryCommand> commands;
	private List<DynamicQueryFilter> subFilters;
	private boolean logic_and = true;

	public DynamicQueryFilter() {
		filterName = "";
		commands = new ArrayList<>();
		paramValues = new ArrayList<>();
		subFilters = new ArrayList<>();
	}
	
    private <T> DynamicQueryFilter addFilter(String fieldName, Operator operator, T value) {
        if (operator == Operator.isNull || operator == Operator.isNotNull) {
            return addFilter(fieldName, operator, Type.String, value);
        }
        Preconditions.checkNotNull(value);
        if (value instanceof String) {
            return addFilter(fieldName, operator, Type.String, (String)value);
        } else if (value instanceof Number) {
            return addFilter(fieldName, operator, Type.Number, (Number)value);
        } else if (value instanceof Date) {
            return addFilter(fieldName, operator, Type.Date, (Date)value);
        } else if (value instanceof Boolean) {
            return addFilter(fieldName, operator, Type.Number, ((Boolean)value).booleanValue() ? 1 : 0);
        } else if (value instanceof ExprEntry) {
            return addFilter(fieldName, operator, Type.Expression, (ExprEntry)value);
        } else {
            throw new RuntimeException("不支持的类型");
        }
    }
    
    /**
     * @param fieldName
     * @param collection
     * @return
     */
    public DynamicQueryFilter in(String fieldName, Collection<?> collection) {
        return addFilter(fieldName, Operator.in, Type.List, collection);
    }
    
    /**
     * 
     * @param fieldName
     * @param collection
     * @return
     */
    public DynamicQueryFilter notIn(String fieldName, Collection<?> collection) {
        return addFilter(fieldName, Operator.notIn, Type.List, collection);
    }
    
    /**
     * 针对1,3,4,5,5,6,11,33形式的串进行检索
     * 但注意类似检索用不到索引，非管理后台的功能请注意加缓存，并评估好数据量
     * 
     * @param fieldName
     * @param collection
     * @return
     */
    public DynamicQueryFilter inJoinedString(String fieldName, String value) {
        return addFilter(fieldName, Operator.inJoinString, Type.String, value);
    }
    
    /**
     * 表达式支持
     * 因为可能产生注入，所以一般禁止在其中拼接变量
     * 
     * @param fieldName
     * @param operator
     * @param expr
     * @return
     */
    public <T> DynamicQueryFilter expr(String fieldName, Operator operator, ExprEntry expr) {
        return addFilter(fieldName, operator, Type.Expression, expr);
    }
    
    public <T> DynamicQueryFilter equalPair(String fieldName, T value) {
        return addFilter(fieldName, Operator.equal, value);
    }
    
    public <T> DynamicQueryFilter greatThan(String fieldName, T value) {
        return addFilter(fieldName, Operator.greatThan, value);
    }
    
    public <T> DynamicQueryFilter lessThan(String fieldName, T value) {
        return addFilter(fieldName, Operator.lessThan, value);
    }
    
    public <T> DynamicQueryFilter notEqual(String fieldName, T value) {
        return addFilter(fieldName, Operator.notEqual, value);
    }
    
    public <T> DynamicQueryFilter startsWith(String fieldName, String value) {
        return addFilter(fieldName, Operator.startsWith, value);
    }
    
    public <T> DynamicQueryFilter endsWith(String fieldName, String value) {
        return addFilter(fieldName, Operator.endsWith, value);
    }
    
    public <T> DynamicQueryFilter like(String fieldName, String value) {
        return addFilter(fieldName, Operator.like, value);
    }
    
    public <T> DynamicQueryFilter greatOrEqual(String fieldName, T value) {
        return addFilter(fieldName, Operator.greatOrEqual, value);
    }
    
    public <T> DynamicQueryFilter lessOrEqual(String fieldName, T value) {
        return addFilter(fieldName, Operator.lessOrEqual, value);
    }
    
    public <T> DynamicQueryFilter isNull(String fieldName) {
        return addFilter(fieldName, Operator.isNull, null);
    }
    
    public <T> DynamicQueryFilter notNull(String fieldName) {
        return addFilter(fieldName, Operator.isNotNull, null);
    }
    
    /**
     * 目前对于数值、字符串、日期、可遍历集合(Collection)、布尔等常见类型做了快捷调用定义
     * 对于更高级的beginOfDay/endOfDay依旧需要调用此全功能方法
     * @param fieldName
     * @param operator
     * @param type
     * @param value
     * @return
     */
    public DynamicQueryFilter addFilter(String fieldName, Operator operator, Type type, Object value) {
        if (operator == Operator.isNull || operator == Operator.isNotNull) {
            FieldCommand fieldCommand = new FieldCommand(fieldName, value, operator, this);
            commands.add(fieldCommand);
            paramValues.add(fieldCommand.getValue());
        } else {
            value = ParamUtil.convertObject(type, value);
            if (value != null) {
                FieldCommand fieldCommand = new FieldCommand(fieldName, value, operator, this);
                commands.add(fieldCommand);
                paramValues.add(fieldCommand.getValue());
            } else {
                throw new RuntimeException("value parsing exception: " + fieldName + " => " + value);
            }
        }
        return this;
    }
    
    /**
     * 本Filter条件关系为and
     * 
     * @return
     */
    public DynamicQueryFilter and() {
        logic_and = true;
        return this;
    }
    
    /**
     * 本Filter条件关系为or
     * 
     * @return
     */
    public DynamicQueryFilter or() {
        logic_and = false;
        return this;
    }
    
    /**
     * 以页码方式来设置分页
     * 
     * @param page 页码
     * @param pageSize 每页大小
     * @return
     */
    public DynamicQueryFilter page(int page, int pageSize) {
        if (page < 1) {
            page = 1;
        }
        if (pageSize < 0) {
            pageSize = 1;
        }
        if (pageSize > 10000) {
            throw new RuntimeException("page size is more than 10000?");
        }
        offset = (page - 1) * pageSize;
        limit = pageSize;
        return this;
    }
    
    /**
     * 直接设置sql:
     * limit ${offset}, ${count}
     * 
     * @param offset
     * @param count
     * @return
     */
    public DynamicQueryFilter limit(int offset, int count) {
        this.offset = offset;
        this.limit = count;
        return this;
    }
    
    /**
     * 设置最多返回的行数
     * 
     * @param count
     * @return
     */
    public DynamicQueryFilter limit(int count) {
        this.offset = 0;
        this.limit = count;
        return this;
    }
    
    /**
     * 添加子过滤器，仅条件有效，排序无效
     * @param filter
     */
    public DynamicQueryFilter addSubFilter(DynamicQueryFilter filter) {
        filter.setFilterName("sub" + subFilters.size());
        subFilters.add(filter);
        return this;
    }
    
    /**
     * 将本Filter作为目标Filter的子条件集合
     * 
     * @param filter
     * @return
     */
    public DynamicQueryFilter addToFilter(DynamicQueryFilter filter) {
        filter.addSubFilter(this);
        return this;
    }

	/**
	 * 内部名字，内部使用
	 * @param filterName
	 */
	private void setFilterName(String filterName) {
		this.filterName = filterName;
	}

	public List<Object> getParamValueList() {
		return this.paramValues;
	}
	
	public QueryData getParameters() {
		return getQueryData();
	}

    public QueryData getParameters(String sql) {
        return getQueryData(sql);
    }

	/**
	 * 组装面向数据库表的SQL
	 * @param cmd
	 * @param parameterMap
	 * @return
	 */
	private String getPartSql(FieldCommand cmd, Map<String, Object> parameterMap) {
		String property = cmd.getProperty();
		Operator operation = cmd.getOperation();
		Object value = cmd.getValue();

		String partHql = "";
		String fieldKey = operation + "_" + property + "_" + (filterName != null && filterName.length() > 0 ? (filterName + "_") : "") + parameterMap.size();
		// to underline format
		String fieldName = StringUtil.toUnderline(property);

		if (value instanceof ExprEntry) {
		    ExprEntry expr = (ExprEntry) value;
		    //特殊处理表达式类型
		    String op = null;
		    switch (operation) {
	            case lessThan:
	                op = " < ";
	                break;
	            case greatThan:
	                op = " > ";
	                break;
	            case greatOrEqual:
	                op = " >= ";
	                break;
	            case lessOrEqual:
	                op = " <= ";
	                break;
	            case like:
	                op = " like ";
	                break;
	            case notEqual:
	                op = " <> ";
	                break;
	            case equal:
	                op = " = ";
	                break;

	            default:
	                throw new RuntimeException("Syntax Invalid:Property=" + property + "\tOperation=" + operation + "\tValue=" +value);
	        }
		    //对字段名字的处理先尝试做纯属性名处理，如转换失败(@符不消失)则采用expr规则
		    String name = null;
		    if (property.contains("@")) {
		        //特殊处理方式
                name = ColumnUtil.parseColumnNames(null, property, expr.getClz());
            }
		    if (name == null) {
		        //普通处理方式
    		    name = fieldName;
		    }
            return String.format("%s%s%s", name, op,
                    ColumnUtil.parseColumnNames(property, expr.getValue(), expr.getClz()));
		}
		String fieldKeyReplace = String.format("#{%s}", fieldKey);
		switch (operation) {
            case lessThan:
                partHql = (new StringBuilder(String.valueOf(fieldName))).append(" <").append(fieldKeyReplace).toString();
                parameterMap.put(fieldKey, value);
                break;
            case greatThan:
                partHql = (new StringBuilder(String.valueOf(fieldName))).append(" > ").append(fieldKeyReplace).toString();
                parameterMap.put(fieldKey, value);
                break;
            case greatOrEqual:
                partHql = (new StringBuilder(String.valueOf(fieldName))).append(" >= ").append(fieldKeyReplace).toString();
                parameterMap.put(fieldKey, value);
                break;
            case lessOrEqual:
                partHql = (new StringBuilder(String.valueOf(fieldName))).append(" <= ").append(fieldKeyReplace).toString();
                parameterMap.put(fieldKey, value);
                break;
            case like:
                partHql = (new StringBuilder(String.valueOf(fieldName))).append(" like ").append(fieldKeyReplace).toString();
                parameterMap.put(fieldKey, "%" + value + "%");
                break;
            case startsWith:
                partHql = (new StringBuilder(String.valueOf(fieldName))).append(" like ").append(fieldKeyReplace).toString();
                parameterMap.put(fieldKey, value + "%");
                break;
            case endsWith:
                partHql = (new StringBuilder(String.valueOf(fieldName))).append(" like ").append(fieldKeyReplace).toString();
                parameterMap.put(fieldKey, "%" + value);
                break;
            case isNull:
                partHql = (new StringBuilder(String.valueOf(fieldName))).append(" is null ").toString();
                break;
            case isNotNull:
                partHql = (new StringBuilder(String.valueOf(fieldName))).append(" is not null ").toString();
                break;
            case inJoinString:
                partHql = String.format("concat(',', `%s`, ',') like %s", fieldName, fieldKeyReplace);
                parameterMap.put(fieldKey, "%," + value + ",%");
                break;
            case in:
            case notIn: {
                List<String> valueList = ParamUtil.getCollection(value);
                StringBuilder sb = new StringBuilder(String.valueOf(fieldName))
                        .append(operation == Operator.notIn ? " not  " : " ").append("in")
                        .append(" ( ");
                for (int i = 0; i < valueList.size(); i++) {
                    if (i != 0) {
                        sb.append(",");
                    }
                    sb.append("#{").append(fieldKey).append("_").append(i).append("}");
                    parameterMap.put(fieldKey + "_" + i, valueList.get(i));
                }
                partHql = sb.append(" )").toString();
                break;
            }
            case notEqual:
                partHql = (new StringBuilder(String.valueOf(fieldName))).append(" !=").append(fieldKeyReplace).toString();
                parameterMap.put(fieldKey, value);
                break;
            case equal:
                partHql = (new StringBuilder(String.valueOf(partHql))).append(fieldName).append(" =")
                        .append(fieldKeyReplace).toString();
                parameterMap.put(fieldKey, value);
                break;
            case lessOrEqualTimestamp:
                partHql = fieldName + " <= FROM_UNIXTIME(" + fieldKeyReplace+")";
                parameterMap.put(fieldKey, value);
                break;
            case greatOrEqualTimestamp:
                partHql = fieldName + " >= FROM_UNIXTIME(" + fieldKeyReplace+")";
                parameterMap.put(fieldKey, value);
                break;

            default:
                throw new RuntimeException("Syntax Invalid:Property=" + property + "\tOperation=" + operation + "\tValue=" +value);
        }
        return partHql;
	}

    public QueryData getQueryData() {
        QueryData map = new QueryData();
        QueryCommand cmd = null;
        StringBuilder where = new StringBuilder();
        StringBuilder orderBy = new StringBuilder();
        for (int i = 0; i < commands.size(); i++) {
            cmd = commands.get(i);
            if (cmd instanceof FieldCommand) {
                if (where.length() > 0) {
                    if (logic_and) {
                        where.append(" and ");
                    } else {
                        where.append(" or ");
                    }
                }
                where.append(getPartSql((FieldCommand) cmd, map));
            } else if (cmd instanceof SortCommandImpl) {
                if (orderBy.length() > 0) {
                    orderBy.append(", ");
                } else {
                    orderBy.append("order by ");
                }
                orderBy.append(((SortCommandImpl)cmd).getPartSql());
            }
        }
        
        //处理子filter，只支持where解析
        for (DynamicQueryFilter sub : subFilters) {
            if (where.length() > 0) {
                if (logic_and) {
                    where.append(" and ");
                } else {
                    where.append(" or ");
                }
            }
            where.append("(");
            Map<String, Object> params = sub.getParameters();
            for (Entry<String, Object> entry : params.entrySet()) {
                if (entry.getKey().equals("SortSQL")) {
                    //do nothing
                } else if (entry.getKey().equals("WhereSQL")) {
                    where.append(entry.getValue());
                } else {
                    //field
                    map.put(entry.getKey(), entry.getValue());
                }
            }
            where.append(")");
        }
        map.put("SearchAll", false);
        if (where.length() < 1) {
            where.append("1=1");
            map.put("SearchAll", true);
        }

        map.put("WhereSQL", where.toString());
        map.put("SortSQL", orderBy.length() > 0 ? orderBy.toString() : null);
        map.setLimit(offset, limit);
        return map;
    }

    public QueryData getQueryData(String sql) {
        QueryData map = getQueryData();
        if(StringUtils.isNotEmpty(sql)){
            StringBuilder builder = new StringBuilder(map.get("WhereSQL").toString());
            builder.append(" ").append(sql);
            map.put("SearchAll", false);
            map.put("WhereSQL", builder.toString());
        }
        return map;
    }

    public DynamicQueryFilter orderBy(String orderBy, boolean isDesc) {
        commands.add(new SortCommandImpl(orderBy, isDesc));
        return this;
    }

}