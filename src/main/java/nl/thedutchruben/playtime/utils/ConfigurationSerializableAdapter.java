package nl.thedutchruben.playtime.utils;

import com.google.gson.*;

import com.google.gson.reflect.TypeToken;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConfigurationSerializableAdapter implements JsonSerializer<ConfigurationSerializable>, JsonDeserializer<ConfigurationSerializable> {

    final Type objectStringMapType = new TypeToken<LinkedHashMap<String, Object>>() {}.getType();

    @Override
    public ConfigurationSerializable deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
            JsonElement value = entry.getValue();
            String name = entry.getKey();

            if (value.isJsonObject()) {
                JsonObject jsonObject = value.getAsJsonObject();
                if(jsonObject.has(ConfigurationSerialization.SERIALIZED_TYPE_KEY)) {
                    map.put(name, deserialize(value, value.getClass(), context));
                } else {
                    LinkedHashMap<String, Object> mapInMap = new LinkedHashMap<>();
                    for (Map.Entry<String, JsonElement> secondEntry : jsonObject.entrySet()) {
                        JsonElement element = secondEntry.getValue();
                        if(element.isJsonObject()) {
                            mapInMap.put(secondEntry.getKey(), deserialize(secondEntry.getValue(), secondEntry.getClass(), context));
                        } else if(element.isJsonArray()) {
                            JsonArray array = element.getAsJsonArray();
                            List<Object> objectsList = new ArrayList<>();
                            for(JsonElement arrayElement : array) {
                                objectsList.add(deserialize(arrayElement, arrayElement.getClass(), context));
                            }
                            mapInMap.put(secondEntry.getKey(), objectsList);
                        } else {
                            mapInMap.put(secondEntry.getKey(), context.deserialize(element, Object.class));
                        }
                    }
                    map.put(name, mapInMap);
                }
            } else if(value.isJsonArray()) {
                JsonArray array = value.getAsJsonArray();
                List<Object> objectsList = new ArrayList<>();
                for(JsonElement element : array) {
                    if(element.isJsonObject() && element.getAsJsonObject().has(ConfigurationSerialization.SERIALIZED_TYPE_KEY)) {
                        objectsList.add(deserialize(element, element.getClass(), context));
                    } else {
                        objectsList.add(context.deserialize(element, Object.class));
                    }
                }
                map.put(name, objectsList);
            } else {
                map.put(name, context.deserialize(value, Object.class));
            }
        }
        return ConfigurationSerialization.deserializeObject(map);
    }

    @Override
    public JsonElement serialize(ConfigurationSerializable src, Type typeOfSrc, JsonSerializationContext context) {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put(ConfigurationSerialization.SERIALIZED_TYPE_KEY, ConfigurationSerialization.getAlias(src.getClass()));
        map.putAll(src.serialize());
        return context.serialize(map, objectStringMapType);
    }

}