/*
 * Copyright (c) 2016 Anton Bulakh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package necauqua.mods.cm;

import sun.reflect.CallerSensitive;
import sun.reflect.Reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.function.Consumer;

public final class RandomUtils {

    private RandomUtils() {}

    @CallerSensitive
    @SuppressWarnings("deprecation")
    public static <T> void forEachStaticField(Class<T> type, Consumer<T> action) {
        forEachStaticField(Reflection.getCallerClass(2), type, action);
    }

    @SuppressWarnings("unchecked")
    public static <T> void forEachStaticField(Class<?> holder, Class<T> type, Consumer<T> action) {
        try {
            for(Field field : holder.getFields()) {
                if((field.getModifiers() & Modifier.STATIC) != 0) {
                    Object obj = field.get(null);
                    if(type.isAssignableFrom(obj.getClass())) {
                        action.accept((T) obj);
                    }
                }
            }
        }catch(IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
