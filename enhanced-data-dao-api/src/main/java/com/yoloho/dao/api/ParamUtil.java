package com.yoloho.dao.api;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.yoloho.dao.api.filter.FieldCommand.Type;



public class ParamUtil {
    private final static String COLLECTION_STR = new String(new byte[]{0x0e, 0x02, (byte) 0xff});
    private final static Joiner COLLECTION_JOINER = Joiner.on(COLLECTION_STR);
    private final static Splitter COLLECTION_SPLITTER = Splitter.on(COLLECTION_STR).trimResults();
    private final static Splitter COLLECTION_SPLITTER_OLD = Splitter.on(",").trimResults();

	public ParamUtil() {
	}
	

	@SuppressWarnings({ "rawtypes" })
    public static Object convertObject(Type type, Object paramValue) {
		Object value = null;
		//只允许字符串类型有长度为0的查询
        if (paramValue == null || paramValue instanceof String) {
            if (Type.String == type && StringUtils.isEmpty((String) paramValue)) {
                paramValue = "";
            } else {
                new RuntimeException("filter value is empty");
            }
        }
		try {
            if (type == Type.String) {
                //string
                if (paramValue instanceof String) {
                    value = (String)paramValue;
                }
				value = paramValue.toString();
			} else if (type == Type.BigDecimal) {
			    //BigDecimal
			    if (paramValue instanceof String) {
                    value = new BigDecimal((String)paramValue);
                } else if (paramValue instanceof BigDecimal) {
                    value = (BigDecimal)paramValue;
                } else if (paramValue instanceof Double) {
                    value = BigDecimal.valueOf((Double)paramValue);
                } else if (paramValue instanceof Float) {
                    value = BigDecimal.valueOf((Float)paramValue);
                } else if (paramValue instanceof Number) {
                    value = BigDecimal.valueOf((Float)paramValue);
                } else {
                    new RuntimeException("value type is not support for bigdecimal type");
                }
			} else if (type == Type.Number) {
			    if (paramValue instanceof Number) {
                    value = paramValue;
                } else {
                    new RuntimeException("value type is not support for number type");
                }
			} else if (type == Type.Date) {
			    //date
                if (paramValue instanceof String) {
                    value = DateUtils.parseDate((String)paramValue, new String[] { "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss" });
                } else if (paramValue instanceof Date) {
                    value = (Date)paramValue;
                } else {
                    new RuntimeException("value type is not support for date type");
                }
			} else if (type == Type.beginOfDay) {
			    if (paramValue instanceof String) {
    				Calendar cal = Calendar.getInstance();
    				cal.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
    				cal.setTime(DateUtils.parseDate((String)paramValue, new String[] { "yyyy-MM-dd" }));
    				value = setStartDay(cal).getTime();
			    } else {
			        new RuntimeException("value type is not support for date (less than) type");
			    }
			} else if (type == Type.endOfDay) {
			    if (paramValue instanceof String) {
    				Calendar cal = Calendar.getInstance();
    				cal.setTime(DateUtils.parseDate((String)paramValue, new String[] { "yyyy-MM-dd" }));
    				value = setEndDay(cal).getTime();
			    } else {
                    new RuntimeException("value type is not support for date (great then) type");
                }
			} else if (type == Type.List || type == Type.Collection) {
			    //list
                if (paramValue instanceof Collection) {
                    value = COLLECTION_JOINER.join((Collection)paramValue);
                } else {
                    new RuntimeException("value type is not support for list type");
                }
			} else if (type == Type.Expression) {
                //list
                if (paramValue instanceof ExprEntry) {
                    value = paramValue;
                } else {
                    new RuntimeException("value type is not support for expr type");
                }
			} else {
				value = paramValue;
			}
		} catch (Exception ex) {
			throw new RuntimeException((new StringBuilder("the data value is not right for the query filed type:")).append(ex.getMessage()).toString());
		}
		return value;
	}
	
	public static List<String> getCollection(Object param) {
	    if (param instanceof String) {
	        String value = (String)param;
	        if (value.indexOf(COLLECTION_STR) >= 0) {
	            //new version
	            return COLLECTION_SPLITTER.splitToList(value);
	        } else {
	            //old version
	            return COLLECTION_SPLITTER_OLD.splitToList(value);
	        }
	    } else {
	        throw new RuntimeException("the data value is not right for the list type");
	    }
	}
	
	public static Calendar setStartDay(Calendar cal) {
		cal.set(11, 0);
		cal.set(12, 0);
		cal.set(13, 0);
		return cal;
	}

	public static Calendar setEndDay(Calendar cal) {
		cal.set(11, 23);
		cal.set(12, 59);
		cal.set(13, 59);
		return cal;
	}
}
