package org.jekh.appenders.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.jekh.appenders.Field;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GsonUtil {
    public static final Type LIST_OF_STRING_TYPE = new TypeToken<List<String>>() {
    }.getType();

    public static final Type SET_OF_STRING_TYPE = new TypeToken<Set<String>>() {
    }.getType();

    public static final Type MAP_OF_STRING_STRING_TYPE = new TypeToken<Map<String, String>>() {
    }.getType();

    public static final Type SET_OF_FIELD_TYPE = new TypeToken<Set<Field>>() {
    }.getType();

    public static final Gson GSON = new GsonBuilder().create();
}
