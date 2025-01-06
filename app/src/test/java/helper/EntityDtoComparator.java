package helper;

import java.lang.reflect.Field;
import java.util.List;

public class EntityDtoComparator {

  public static boolean areEqual(Object entity, Object dto) {
    if (entity == null && dto == null) {
      return true;
    }
    if (entity == null || dto == null) {
      return false;
    }

    Field[] entityFields = entity.getClass().getDeclaredFields();
    Field[] dtoFields = dto.getClass().getDeclaredFields();

    for (Field entityField : entityFields) {
      try {
        // Find a matching field in the DTO
        Field dtoField = findFieldByName(dtoFields, entityField.getName());
        if (dtoField == null) {
          // Skip if no matching field in DTO
          continue;
        }

        entityField.setAccessible(true);
        dtoField.setAccessible(true);

        Object entityValue = entityField.get(entity);
        Object dtoValue = dtoField.get(dto);

        // Handle simple types
        if (isSimpleType(entityField.getType())) {
          if (!compareSimpleValues(entityValue, dtoValue)) {
            return false;
          }
        }
        // Handle List types
        else if (List.class.isAssignableFrom(entityField.getType())
            && List.class.isAssignableFrom(dtoField.getType())) {
          if (!compareListSizes(entityValue, dtoValue)) {
            return false;
          }
        }
        // Skip other complex types
        else {
          continue;
        }

      } catch (IllegalAccessException e) {
        throw new RuntimeException("Failed to access field value", e);
      }
    }

    return true;
  }

  // Utility method to find a field by name in an array of fields
  private static Field findFieldByName(Field[] fields, String fieldName) {
    for (Field field : fields) {
      if (field.getName().equals(fieldName)) {
        return field;
      }
    }
    return null;
  }

  // Utility method to compare simple types
  private static boolean compareSimpleValues(Object entityValue, Object dtoValue) {
    if (entityValue == null && dtoValue == null) {
      return true;
    }
    if (entityValue == null || dtoValue == null) {
      return false;
    }
    return entityValue.equals(dtoValue);
  }

  // Utility method to compare List sizes
  private static boolean compareListSizes(Object entityValue, Object dtoValue) {
    if (entityValue == null && dtoValue == null) {
      return true;
    }
    if (entityValue == null || dtoValue == null) {
      return false;
    }
    List<?> entityList = (List<?>) entityValue;
    List<?> dtoList = (List<?>) dtoValue;
    return entityList.size() == dtoList.size();
  }

  // Utility method to determine if a type is simple (primitive, wrapper, or String)
  private static boolean isSimpleType(Class<?> type) {
    return type.isPrimitive()
        || type.equals(String.class)
        || type.equals(Boolean.class)
        || type.equals(Byte.class)
        || type.equals(Character.class)
        || type.equals(Short.class)
        || type.equals(Integer.class)
        || type.equals(Long.class)
        || type.equals(Float.class)
        || type.equals(Double.class)
        || type.equals(Void.class);
  }
}
