package com.ophis.beantranslater.tools.utils;

import com.baomidou.mybatisplus.annotation.TableField;

import java.lang.reflect.Field;

public class TableFieldUtils {
    public static String propertyToColumn(String property, Class entityClass) {
        Field field = null;
        try {
            field = entityClass.getDeclaredField(property);
        } catch (NoSuchFieldException e) {
            return null;
        }
        TableField annotation = field.getAnnotation(TableField.class);
        if(annotation !=null){
            return annotation.value();
        }
        return property;
    }
}
