package com.ophis.beantranslater.tools;

import com.ophis.beantranslater.tools.lambda.LambdaUtils;
import com.ophis.beantranslater.tools.lambda.SerializedFunction;
import lombok.Data;

import java.util.List;

@Data
public class TransMapping<T,R> {

    List<String> translatedList;
    List<String> foreignList;

    public static TransMapping build(){
        return new TransMapping();
    }

    public static <T,R> TransMapping<T,R> build(Class<T> source,Class<R> foreign){
        return new TransMapping<T,R>();
    }
    public TransMapping map(String translated,String foreignName){
        translatedList.add(translated);
        foreignList.add(foreignName);
        return this;
    }

    /**
     *
     * @param translated    转换后的结果放哪个字段
     * @param foreignName   转换使用远程或外连对象的哪个字段
     * @return
     */
    public TransMapping<T,R> map(SerializedFunction<T, ?> translated , SerializedFunction<R, ?> foreignName){
        translatedList.add(LambdaUtils.property(translated));
        foreignList.add(LambdaUtils.property(foreignName));
        return this;
    }


}
