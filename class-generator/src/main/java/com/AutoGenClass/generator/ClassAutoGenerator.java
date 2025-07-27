package com.AutoGenClass.generator;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.*;
import java.util.*;

/**
 * Annotation processor for @AutoGen annotation.
 * Generates DTO classes at compile time based on the annotation configuration.
 * 
 * <p>This processor reads the @AutoGen annotation from source classes and generates
 * corresponding DTO classes with proper types, imports, and Jackson serialization annotations.
 * It supports collection types, entity dependencies, and automatic import generation.</p>
 * 
 * <p>Key features:</p>
 * <ul>
 *   <li>Type-safe DTO generation with proper field types</li>
 *   <li>Collection support (List, Set, Map, arrays)</li>
 *   <li>Automatic import generation based on field types</li>
 *   <li>Jackson serialization annotations</li>
 *   <li>Entity dependency handling</li>
 * </ul>
 * 
 * @author Mohammed-Salameh
 * @version 2.0
 * @since 1.0
 */
@SupportedAnnotationTypes("com.AutoGenClass.generator.AutoGen")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class ClassAutoGenerator extends AbstractProcessor {

    /** Messager for reporting compilation messages and errors */
    private Messager messager;

    /** Filer for creating source files */
    private Filer filer;

    /**
     * Initializes the annotation processor with the processing environment.
     * 
     * <p>This method is called once before processing begins. It sets up the
     * messager for reporting and the filer for creating source files.</p>
     * 
     * @param processingEnv the processing environment provided by the compiler
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.messager = processingEnv.getMessager();
        this.filer = processingEnv.getFiler();
    }

    /**
     * Processes the annotations during compilation.
     * 
     * <p>This is the main entry point for the annotation processor. It processes
     * all classes annotated with @AutoGen and generates corresponding DTO classes.</p>
     * 
     * @param annotations the set of annotation types being processed
     * @param roundEnv the environment for the current processing round
     * @return true if the annotations are claimed by this processor, false otherwise
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // Skip processing if this is the final round
        if (roundEnv.processingOver()) {
            return false;
        }

        // Process each annotation type
        for (TypeElement annotation : annotations) {
            Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);
            // Process each annotated element
            for (Element element : annotatedElements) {
                // Only process classes and interfaces
                if (element.getKind() == ElementKind.CLASS || element.getKind() == ElementKind.INTERFACE) {
                    processClass((TypeElement) element);
                }
            }
        }
        
        return true;
    }

    /**
     * Processes a single annotated class and generates its DTO.
     * 
     * <p>This method extracts the @AutoGen annotation from the class, analyzes its fields,
     * and generates a corresponding DTO class with proper types and annotations.</p>
     * 
     * @param classElement the class element to process
     */
    private void processClass(TypeElement classElement) {
        // Get the @AutoGen annotation from the class
        //System.out.println("processing class : " + classElement);
        AutoGen autoGen = classElement.getAnnotation(AutoGen.class);
        if (autoGen == null) {
            //System.out.println("class : " + classElement + " is annotated but not with autogen");
            messager.printMessage(Diagnostic.Kind.NOTE , "class : " + classElement + " is annotated but not with autogen");
            return;
        }

        messager.printMessage(Diagnostic.Kind.NOTE,"processing class : " + classElement);
        // Extract annotation parameters
        String className = autoGen.name();
        String packageName = getPackageName(classElement);
        String[] simpleFields = autoGen.simpleFields();
        String[] serializedFields = autoGen.serializedFields();
        String[] serializers = autoGen.serializers();
        
        try {
            // Analyze field types using annotation processing API
            //System.out.println("class : " + classElement + " is annotated but not with autogen");
            Map<String, FieldInfo> fieldInfoMap = getFieldInfo(classElement, simpleFields, serializedFields);
            
            // Generate the DTO class
            generateDTOClass(classElement, className, packageName, simpleFields, serializedFields, serializers, fieldInfoMap);
            messager.printMessage(Diagnostic.Kind.NOTE, "Generated DTO class: " + packageName + "." + className);
        } catch (Exception e) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Failed to generate DTO class: " + e.getMessage());
        }
    }

    /**
     * Container class for field type information.
     * 
     * <p>This class holds comprehensive information about a field's type,
     * including whether it's a collection, its element types, and import requirements.</p>
     */
    private static class FieldInfo {
        /** Simple type name (e.g., "List", "String") */
        String typeName;
        
        /** Full qualified type name (e.g., "java.util.List", "java.lang.String") */
        String fullTypeName;
        
        /** Whether this field is a collection type */
        boolean isCollection;
        
        /** Element type for collections (e.g., "String" for List<String>) */
        String collectionElementType;
        
        /** Whether this field is a Map type */
        boolean isMap;
        
        /** Key type for Map collections (e.g., "String" for Map<String, User>) */
        String mapKeyType;
        
        /** Value type for Map collections (e.g., "User" for Map<String, User>) */
        String mapValueType;
        
        /** Whether this field is an entity type */
        boolean isEntity;
        
        /** Whether this type needs an import statement */
        boolean needsImport;

        /**
         * Creates a new FieldInfo instance.
         * 
         * @param typeName the simple type name
         * @param fullTypeName the full qualified type name
         * @param isCollection whether this is a collection type
         * @param collectionElementType the element type for collections
         * @param isMap whether this is a Map type
         * @param mapKeyType the key type for Map collections
         * @param mapValueType the value type for Map collections
         * @param isEntity whether this is an entity type
         * @param needsImport whether this type needs an import
         */
        FieldInfo(String typeName, String fullTypeName, boolean isCollection, String collectionElementType, 
                  boolean isMap, String mapKeyType, String mapValueType, boolean isEntity, boolean needsImport) {
            this.typeName = typeName;
            this.fullTypeName = fullTypeName;
            this.isCollection = isCollection;
            this.collectionElementType = collectionElementType;
            this.isMap = isMap;
            this.mapKeyType = mapKeyType;
            this.mapValueType = mapValueType;
            this.isEntity = isEntity;
            this.needsImport = needsImport;
        }
    }

    /**
     * Analyzes the fields of a class and creates FieldInfo objects for each field.
     * 
     * <p>This method matches annotation field names with actual class fields and
     * analyzes their types to determine proper DTO field types and import requirements.</p>
     * 
     * @param classElement the class element to analyze
     * @param simpleFields array of simple field names from the annotation
     * @param serializedFields array of serialized field names from the annotation
     * @return a map of field names to their FieldInfo objects
     */
    private Map<String, FieldInfo> getFieldInfo(TypeElement classElement, String[] simpleFields, String[] serializedFields) {
        Map<String, FieldInfo> fieldInfoMap = new HashMap<>();
        
        // Combine all field names from both simple and serialized fields
        Set<String> allFieldNames = new HashSet<>();
        allFieldNames.addAll(Arrays.asList(simpleFields));
        allFieldNames.addAll(Arrays.asList(serializedFields));
        
        // Get all declared fields from the class using annotation processing API
        Map<String, VariableElement> fieldMap = new HashMap<>();
        
        for (Element member : classElement.getEnclosedElements()) {
            if (member.getKind() == ElementKind.FIELD) {
                VariableElement field = (VariableElement) member;
                fieldMap.put(field.getSimpleName().toString(), field);
            }
        }
        
        // Match annotation fields with actual class fields and analyze their types
        for (String fieldName : allFieldNames) {
            VariableElement field = fieldMap.get(fieldName);
            if (field != null) {
                TypeMirror fieldType = field.asType();
                FieldInfo fieldInfo = analyzeFieldType(fieldType);
                fieldInfoMap.put(fieldName, fieldInfo);
            } else {
                // If field not found, default to Object and log a warning
                fieldInfoMap.put(fieldName, new FieldInfo("Object", "java.lang.Object", false, null, false, null, null, false, false));
                messager.printMessage(Diagnostic.Kind.WARNING, 
                    "Field '" + fieldName + "' not found in class " + classElement.getSimpleName() + 
                    ". Using Object type.");
            }
        }
        
        return fieldInfoMap;
    }

    /**
     * Analyzes a field type and creates a FieldInfo object.
     * 
     * <p>This method determines the type characteristics including whether it's a primitive,
     * wrapper, collection, array, or entity type. It extracts generic type information
     * for collections and determines import requirements.</p>
     * 
     * @param typeMirror the type mirror representing the field type
     * @return a FieldInfo object with comprehensive type information
     */
    private FieldInfo analyzeFieldType(TypeMirror typeMirror) {
        String typeName = typeMirror.toString();
        String fullTypeName = typeMirror.toString();
        
        // Handle primitive types (int, long, double, etc.)
        if (isPrimitiveType(typeName)) {
            return new FieldInfo(getPrimitiveTypeName(typeName), fullTypeName, false, null, false, null, null, false, false);
        }
        
        // Handle wrapper types (Integer, Long, String, etc.)
        if (isWrapperType(typeName)) {
            return new FieldInfo(getWrapperTypeName(typeName), fullTypeName, false, null, false, null, null, false, false);
        }
        
        // Handle collection types (List, Set, Map, etc.)
        if (isCollectionType(typeName)) {
            String elementType = extractCollectionElementType(typeName);
            String simpleElementType = getSimpleTypeName(elementType);
            boolean isMap = typeName.startsWith("java.util.Map");
            String mapKeyType = null;
            String mapValueType = null;
            
            if (isMap) {
                String[] mapTypes = extractMapTypes(typeName);
                mapKeyType = getSimpleTypeName(mapTypes[0]);
                mapValueType = getSimpleTypeName(mapTypes[1]);
            }
            
            return new FieldInfo(getCollectionTypeName(typeName), fullTypeName, true, simpleElementType, isMap, mapKeyType, mapValueType, false, true);
        }
        
        // Handle arrays (String[], User[], etc.)
        if (typeName.endsWith("[]")) {
            String elementType = typeName.substring(0, typeName.length() - 2);
            String simpleElementType = getSimpleTypeName(elementType);
            return new FieldInfo(simpleElementType + "[]", fullTypeName, true, simpleElementType, false, null, null, false, true);
        }
        
        // Handle other types (potentially entities from external JARs)
        String simpleTypeName = getSimpleTypeName(typeName);
        boolean isEntity = !isJavaLangType(typeName);
        boolean needsImport = isEntity && !typeName.contains("java.lang.");
        
        return new FieldInfo(simpleTypeName, fullTypeName, false, null, false, null, null, isEntity, needsImport);
    }

    /**
     * Checks if a type name represents a primitive type.
     * 
     * @param typeName the type name to check
     * @return true if the type is primitive, false otherwise
     */
    private boolean isPrimitiveType(String typeName) {
        return typeName.equals("int") || typeName.equals("long") || typeName.equals("double") || 
               typeName.equals("float") || typeName.equals("boolean") || typeName.equals("char") || 
               typeName.equals("byte") || typeName.equals("short");
    }

    /**
     * Gets the primitive type name (same as input for primitives).
     * 
     * @param typeName the primitive type name
     * @return the primitive type name
     */
    private String getPrimitiveTypeName(String typeName) {
        return typeName;
    }

    /**
     * Checks if a type name represents a wrapper type.
     * 
     * @param typeName the type name to check
     * @return true if the type is a wrapper, false otherwise
     */
    private boolean isWrapperType(String typeName) {
        return typeName.equals("java.lang.Integer") || typeName.equals("java.lang.Long") || 
               typeName.equals("java.lang.Double") || typeName.equals("java.lang.Float") || 
               typeName.equals("java.lang.Boolean") || typeName.equals("java.lang.Character") || 
               typeName.equals("java.lang.Byte") || typeName.equals("java.lang.Short") || 
               typeName.equals("java.lang.String") || typeName.equals("java.lang.Object");
    }

    /**
     * Gets the simple wrapper type name from the full qualified name.
     * 
     * @param typeName the full qualified wrapper type name
     * @return the simple wrapper type name
     */
    private String getWrapperTypeName(String typeName) {
        switch (typeName) {
            case "java.lang.Integer": return "Integer";
            case "java.lang.Long": return "Long";
            case "java.lang.Double": return "Double";
            case "java.lang.Float": return "Float";
            case "java.lang.Boolean": return "Boolean";
            case "java.lang.Character": return "Character";
            case "java.lang.Byte": return "Byte";
            case "java.lang.Short": return "Short";
            case "java.lang.String": return "String";
            case "java.lang.Object": return "Object";
            default: return typeName;
        }
    }

    /**
     * Checks if a type name represents a collection type.
     * 
     * @param typeName the type name to check
     * @return true if the type is a collection, false otherwise
     */
    private boolean isCollectionType(String typeName) {
        return typeName.startsWith("java.util.List") || typeName.startsWith("java.util.Set") || 
               typeName.startsWith("java.util.Collection") || typeName.startsWith("java.util.Map") ||
               typeName.startsWith("java.util.ArrayList") || typeName.startsWith("java.util.HashSet") ||
               typeName.startsWith("java.util.LinkedList") || typeName.startsWith("java.util.TreeSet") ||
               typeName.startsWith("java.util.HashMap") || typeName.startsWith("java.util.LinkedHashMap");
    }

    /**
     * Gets the simple collection type name from the full qualified name.
     * 
     * @param typeName the full qualified collection type name
     * @return the simple collection type name (List, Set, Map, etc.)
     */
    private String getCollectionTypeName(String typeName) {
        if (typeName.startsWith("java.util.List")) return "List";
        if (typeName.startsWith("java.util.Set")) return "Set";
        if (typeName.startsWith("java.util.Collection")) return "Collection";
        if (typeName.startsWith("java.util.Map")) return "Map";
        if (typeName.startsWith("java.util.ArrayList")) return "List";
        if (typeName.startsWith("java.util.HashSet")) return "Set";
        if (typeName.startsWith("java.util.LinkedList")) return "List";
        if (typeName.startsWith("java.util.TreeSet")) return "Set";
        if (typeName.startsWith("java.util.HashMap")) return "Map";
        if (typeName.startsWith("java.util.LinkedHashMap")) return "Map";
        return typeName;
    }

    /**
     * Extracts the element type from a generic collection type.
     * 
     * <p>This method parses generic types like List<String>, Set<User>, etc.
     * and extracts the element type(s). For maps, it returns the key type.</p>
     * 
     * @param typeName the generic collection type name
     * @return the element type name, or "Object" if not found
     */
    private String extractCollectionElementType(String typeName) {
        // Handle generic types like List<String>, Set<User>, etc.
        int startBracket = typeName.indexOf('<');
        int endBracket = typeName.lastIndexOf('>');
        
        if (startBracket != -1 && endBracket != -1) {
            String genericPart = typeName.substring(startBracket + 1, endBracket);
            // Handle multiple generic parameters like Map<String, User>
            String[] parts = genericPart.split(",");
            return parts[0].trim();
        }
        
        return "Object";
    }

    /**
     * Extracts key and value types from a Map type string.
     * 
     * @param typeName the Map type string (e.g., "java.util.Map<java.lang.String,com.example.User>")
     * @return array containing [keyType, valueType]
     */
    private String[] extractMapTypes(String typeName) {
        int startBracket = typeName.indexOf('<');
        int endBracket = typeName.lastIndexOf('>');
        
        if (startBracket != -1 && endBracket != -1) {
            String genericPart = typeName.substring(startBracket + 1, endBracket);
            String[] parts = genericPart.split(",");
            if (parts.length >= 2) {
                return new String[]{parts[0].trim(), parts[1].trim()};
            }
        }
        
        return new String[]{"Object", "Object"};
    }

    /**
     * Checks if a type name is from the java.lang package.
     * 
     * @param typeName the type name to check
     * @return true if the type is from java.lang, false otherwise
     */
    private boolean isJavaLangType(String typeName) {
        return typeName.startsWith("java.lang.") || isPrimitiveType(typeName);
    }

    /**
     * Gets the simple type name from a full qualified name.
     * 
     * @param typeName the full qualified type name
     * @return the simple type name (everything after the last dot)
     */
    private String getSimpleTypeName(String typeName) {
        if (typeName.contains(".")) {
            return typeName.substring(typeName.lastIndexOf(".") + 1);
        }
        return typeName;
    }

    /**
     * Generates the complete DTO class source code.
     * 
     * <p>This method creates the DTO class file with package declaration, imports,
     * class declaration, fields, constructors, getters/setters, and utility methods.</p>
     * 
     * @param sourceClass the source class element
     * @param className the name of the DTO class to generate
     * @param packageName the package name for the DTO
     * @param simpleFields array of simple field names
     * @param serializedFields array of serialized field names
     * @param serializers array of serializer classes
     * @param fieldInfoMap map of field names to their type information
     * @throws IOException if there's an error writing the source file
     */
    private void generateDTOClass(TypeElement sourceClass, String className, String packageName, 
                                 String[] simpleFields, String[] serializedFields, String[] serializers,
                                 Map<String, FieldInfo> fieldInfoMap) 
            throws IOException {
        
        // Create the DTO package name based on groupId
        String dtoPackageName = getDTOPackageName(packageName);
        
        // Generate the source code content
        String sourceCode = generateSourceCode(sourceClass, className, dtoPackageName, 
                                             simpleFields, serializedFields, serializers, fieldInfoMap);
        
        // Write only to source directory (skip filer to avoid recreation issues)
        writeToSourceDirectory(sourceClass, dtoPackageName, className, sourceCode);
    }

    /**
     * Generates the complete source code for a DTO class.
     */
    private String generateSourceCode(TypeElement sourceClass, String className, String packageName,
                                    String[] simpleFields, String[] serializedFields, String[] serializers,
                                    Map<String, FieldInfo> fieldInfoMap) {
        StringBuilder sourceCode = new StringBuilder();
        
        // Generate package declaration
        sourceCode.append("package ").append(packageName).append(";\n\n");
        
        // Generate imports
        sourceCode.append(generateImportsString(fieldInfoMap, serializers));
        
        // Generate class documentation and declaration
        sourceCode.append("/**\n");
        sourceCode.append(" * Auto-generated DTO class for ").append(sourceClass.getQualifiedName()).append("\n");
        sourceCode.append(" */\n");
        sourceCode.append("public class ").append(className).append(" implements Serializable {\n");
        
        // Generate fields
        sourceCode.append(generateFieldsString(simpleFields, serializedFields, serializers, fieldInfoMap));
        
        // Generate constructors
        sourceCode.append(generateConstructorsString(className, simpleFields, serializedFields, fieldInfoMap));
        
        // Generate getters and setters
        sourceCode.append(generateGettersAndSettersString(simpleFields, serializedFields, fieldInfoMap));
        
        // Generate utility methods
        sourceCode.append(generateUtilityMethodsString(className, simpleFields, serializedFields));
        
        sourceCode.append("}\n");
        
        return sourceCode.toString();
    }

    /**
     * Writes the source code to the annotation processor filer.
     */
    private void writeToFiler(String packageName, String className, String sourceCode) throws IOException {
        JavaFileObject sourceFile = filer.createSourceFile(packageName + "." + className);
        try (PrintWriter out = new PrintWriter(sourceFile.openWriter())) {
            out.print(sourceCode);
        }
    }

    /**
     * Writes the source code to the source directory.
     */
    private void writeToSourceDirectory(TypeElement sourceClass, String packageName, String className, String sourceCode) {
        try {
            // Get the source file location from the annotated class
            String sourceDir = findSourceDirectoryFromClass(sourceClass, packageName);
            String packagePath = packageName.replace('.', '/');
            String fullPath = sourceDir + "/" + packagePath;
            
            // Create directories if they don't exist
            File dir = new File(fullPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            // Delete existing file if it exists
            File sourceFile = new File(fullPath + "/" + className + ".java");
            if (sourceFile.exists()) {
                sourceFile.delete();
                messager.printMessage(Diagnostic.Kind.NOTE, 
                    "Deleted existing file: " + sourceFile.getAbsolutePath());
            }
            
            // Write the new source file
            try (PrintWriter out = new PrintWriter(new FileWriter(sourceFile))) {
                out.print(sourceCode);
            }
            
            messager.printMessage(Diagnostic.Kind.NOTE, 
                "Generated source file: " + sourceFile.getAbsolutePath());
                
        } catch (IOException e) {
            messager.printMessage(Diagnostic.Kind.WARNING, 
                "Failed to write source file: " + e.getMessage());
        }
    }

    /**
     * Finds the project root directory by looking for pom.xml.
     */
    private String findProjectRoot(String currentDir) {
        File current = new File(currentDir);
        while (current != null) {
            if (new File(current, "pom.xml").exists()) {
                return current.getAbsolutePath();
            }
            current = current.getParentFile();
        }
        return System.getProperty("user.dir");
    }

    /**
     * Generates import statements as a string.
     */
    private String generateImportsString(Map<String, FieldInfo> fieldInfoMap, String[] serializers) {
        StringBuilder imports = new StringBuilder();

        //Standard imports that are always needed
        imports.append("import com.fasterxml.jackson.annotation.JsonProperty;\n");
        imports.append("import com.fasterxml.jackson.databind.annotation.JsonSerialize;\n");
        imports.append("import java.io.Serializable;\n");
        imports.append("import java.util.Objects;\n");
        
        // Add serializer imports
        Set<String> importSet = new HashSet<>();
        if (serializers != null) {
            for (String serializer : serializers) {
                if (serializer != null && !serializer.trim().isEmpty()) {
                    importSet.add("import " + serializer + ";");
                }
            }
        }
        
        // Collect imports based on field types
        for (FieldInfo fieldInfo : fieldInfoMap.values()) {
            // Add collection imports (only the base collection type, not with generics)
            if (fieldInfo.isCollection) {
                importSet.add("import java.util." + fieldInfo.typeName + ";");
            }
            // Add entity imports for non-java.lang types (only if it's not a collection element type)
            if (fieldInfo.needsImport && !fieldInfo.fullTypeName.startsWith("java.lang.") && !fieldInfo.isCollection) {
                importSet.add("import " + fieldInfo.fullTypeName + ";");
            }
        }
        
        // Write all collected imports
        for (String importStatement : importSet) {
            imports.append(importStatement).append("\n");
        }
        imports.append("\n");
        
        return imports.toString();
    }

    /**
     * Generates field declarations as a string.
     */
    private String generateFieldsString(String[] simpleFields, String[] serializedFields, 
                                      String[] serializers, Map<String, FieldInfo> fieldInfoMap) {
        StringBuilder fields = new StringBuilder();
        
        // Generate simple fields (no special serialization)
        for (String fieldName : simpleFields) {
            FieldInfo fieldInfo = fieldInfoMap.getOrDefault(fieldName, new FieldInfo("Object", "java.lang.Object", false, null, false, null, null, false, false));
            fields.append("    @JsonProperty(\"").append(fieldName).append("\")\n");
            
            // Add content serialization for collections
            if (fieldInfo.isCollection) {
                fields.append("    @JsonSerialize(contentUsing = StdSerializer.class)\n");
                if (fieldInfo.isMap) {
                    fields.append("    private ").append(fieldInfo.typeName).append("<").append(fieldInfo.mapKeyType).append(", ").append(fieldInfo.mapValueType).append("> ").append(fieldName).append(";\n");
                } else {
                    fields.append("    private ").append(fieldInfo.typeName).append("<").append(fieldInfo.collectionElementType).append("> ").append(fieldName).append(";\n");
                }
            } else {
                fields.append("    private ").append(fieldInfo.typeName).append(" ").append(fieldName).append(";\n");
            }
            fields.append("\n");
        }

        // Generate serialized fields (with custom serializers)
        for (int i = 0; i < serializedFields.length && i < serializers.length; i++) {
            String fieldName = serializedFields[i];
            String serializer = serializers[i];
            FieldInfo fieldInfo = fieldInfoMap.getOrDefault(fieldName, new FieldInfo("Object", "java.lang.Object", false, null, false, null, null, false, false));
            
            fields.append("    @JsonProperty(\"").append(fieldName).append("\")\n");
            if (fieldInfo.isCollection) {
                // For collections, add both field and content serialization
                fields.append("    @JsonSerialize(using = ").append(getSimpleClassName(serializer)).append(".class)\n");
                fields.append("    @JsonSerialize(contentUsing = StdSerializer.class)\n");
                if (fieldInfo.isMap) {
                    fields.append("    private ").append(fieldInfo.typeName).append("<").append(fieldInfo.mapKeyType).append(", ").append(fieldInfo.mapValueType).append("> ").append(fieldName).append(";\n");
                } else {
                    fields.append("    private ").append(fieldInfo.typeName).append("<").append(fieldInfo.collectionElementType).append("> ").append(fieldName).append(";\n");
                }
            } else {
                // For simple types, add only field serialization
                fields.append("    @JsonSerialize(using = ").append(getSimpleClassName(serializer)).append(".class)\n");
                fields.append("    private ").append(fieldInfo.typeName).append(" ").append(fieldName).append(";\n");
            }
            fields.append("\n");
        }
        
        return fields.toString();
    }

    /**
     * Extracts the simple class name from a full class name.
     * 
     * @param fullClassName the full class name (e.g., "com.example.MySerializer")
     * @return the simple class name (e.g., "MySerializer")
     */
    private String getSimpleClassName(String fullClassName) {
        if (fullClassName == null || fullClassName.trim().isEmpty()) {
            return "StdSerializer";
        }
        
        int lastDotIndex = fullClassName.lastIndexOf('.');
        if (lastDotIndex >= 0 && lastDotIndex < fullClassName.length() - 1) {
            return fullClassName.substring(lastDotIndex + 1);
        }
        
        return fullClassName;
    }

    /**
     * Generates constructors as a string.
     */
    private String generateConstructorsString(String className, String[] simpleFields, 
                                            String[] serializedFields, Map<String, FieldInfo> fieldInfoMap) {
        StringBuilder constructors = new StringBuilder();
        
        // Generate default constructor
        constructors.append("    public ").append(className).append("() {\n");
        constructors.append("    }\n\n");

        // Generate all-args constructor
        constructors.append("    public ").append(className).append("(\n");
        
        // Add simple field parameters
        for (int i = 0; i < simpleFields.length; i++) {
            String fieldName = simpleFields[i];
            FieldInfo fieldInfo = fieldInfoMap.getOrDefault(fieldName, new FieldInfo("Object", "java.lang.Object", false, null, false, null, null, false, false));
            String paramType;
            if (fieldInfo.isCollection) {
                if (fieldInfo.isMap) {
                    paramType = fieldInfo.typeName + "<" + fieldInfo.mapKeyType + ", " + fieldInfo.mapValueType + ">";
                } else {
                    paramType = fieldInfo.typeName + "<" + fieldInfo.collectionElementType + ">";
                }
            } else {
                paramType = fieldInfo.typeName;
            }
            constructors.append("        ").append(paramType).append(" ").append(fieldName);
            if (i < simpleFields.length - 1 || serializedFields.length > 0) {
                constructors.append(",\n");
            } else {
                constructors.append("\n");
            }
        }
        
        // Add serialized field parameters
        for (int i = 0; i < serializedFields.length; i++) {
            String fieldName = serializedFields[i];
            FieldInfo fieldInfo = fieldInfoMap.getOrDefault(fieldName, new FieldInfo("Object", "java.lang.Object", false, null, false, null, null, false, false));
            String paramType;
            if (fieldInfo.isCollection) {
                if (fieldInfo.isMap) {
                    paramType = fieldInfo.typeName + "<" + fieldInfo.mapKeyType + ", " + fieldInfo.mapValueType + ">";
                } else {
                    paramType = fieldInfo.typeName + "<" + fieldInfo.collectionElementType + ">";
                }
            } else {
                paramType = fieldInfo.typeName;
            }
            constructors.append("        ").append(paramType).append(" ").append(fieldName);
            if (i < serializedFields.length - 1) {
                constructors.append(",\n");
            } else {
                constructors.append("\n");
            }
        }
        
        // Constructor body - assign parameters to fields
        constructors.append("    ) {\n");
        for (String fieldName : simpleFields) {
            constructors.append("        this.").append(fieldName).append(" = ").append(fieldName).append(";\n");
        }
        for (String fieldName : serializedFields) {
            constructors.append("        this.").append(fieldName).append(" = ").append(fieldName).append(";\n");
        }
        constructors.append("    }\n\n");
        
        return constructors.toString();
    }

    /**
     * Generates getters and setters as a string.
     */
    private String generateGettersAndSettersString(String[] simpleFields, String[] serializedFields, 
                                                  Map<String, FieldInfo> fieldInfoMap) {
        StringBuilder methods = new StringBuilder();
        
        // Generate getters and setters for simple fields
        for (String fieldName : simpleFields) {
            methods.append(generateGetterAndSetterString(fieldName, fieldInfoMap.getOrDefault(fieldName, new FieldInfo("Object", "java.lang.Object", false, null, false, null, null, false, false))));
        }
        
        // Generate getters and setters for serialized fields
        for (String fieldName : serializedFields) {
            methods.append(generateGetterAndSetterString(fieldName, fieldInfoMap.getOrDefault(fieldName, new FieldInfo("Object", "java.lang.Object", false, null, false, null, null, false, false))));
        }
        
        return methods.toString();
    }

    /**
     * Generates a getter and setter method for a single field as a string.
     */
    private String generateGetterAndSetterString(String fieldName, FieldInfo fieldInfo) {
        StringBuilder methods = new StringBuilder();
        String capitalizedFieldName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        String returnType;
        if (fieldInfo.isCollection) {
            if (fieldInfo.isMap) {
                returnType = fieldInfo.typeName + "<" + fieldInfo.mapKeyType + ", " + fieldInfo.mapValueType + ">";
            } else {
                returnType = fieldInfo.typeName + "<" + fieldInfo.collectionElementType + ">";
            }
        } else {
            returnType = fieldInfo.typeName;
        }
        
        // Generate getter method
        methods.append("    public ").append(returnType).append(" get").append(capitalizedFieldName).append("() {\n");
        methods.append("        return ").append(fieldName).append(";\n");
        methods.append("    }\n\n");

        // Generate setter method
        methods.append("    public void set").append(capitalizedFieldName).append("(").append(returnType).append(" ").append(fieldName).append(") {\n");
        methods.append("        this.").append(fieldName).append(" = ").append(fieldName).append(";\n");
        methods.append("    }\n\n");
        
        return methods.toString();
    }

    /**
     * Generates utility methods as a string.
     */
    private String generateUtilityMethodsString(String className, String[] simpleFields, String[] serializedFields) {
        StringBuilder methods = new StringBuilder();
        
        // Generate equals method
        methods.append("    @Override\n");
        methods.append("    public boolean equals(Object obj) {\n");
        methods.append("        if (this == obj) return true;\n");
        methods.append("        if (obj == null || getClass() != obj.getClass()) return false;\n");
        methods.append("        ").append(className).append(" that = (").append(className).append(") obj;\n");
        methods.append("        return \n");
        
        // Combine all fields for comparison
        String[] allFields = new String[simpleFields.length + serializedFields.length];
        System.arraycopy(simpleFields, 0, allFields, 0, simpleFields.length);
        System.arraycopy(serializedFields, 0, allFields, simpleFields.length, serializedFields.length);
        
        // Generate field comparisons
        for (int i = 0; i < allFields.length; i++) {
            methods.append("                Objects.equals(").append(allFields[i]).append(", that.").append(allFields[i]).append(")");
            if (i < allFields.length - 1) {
                methods.append(" &&\n");
            } else {
                methods.append(";\n");
            }
        }
        methods.append("    }\n\n");

        // Generate hashCode method
        methods.append("    @Override\n");
        methods.append("    public int hashCode() {\n");
        methods.append("        return Objects.hash(\n");
        for (int i = 0; i < allFields.length; i++) {
            methods.append("            ").append(allFields[i]);
            if (i < allFields.length - 1) {
                methods.append(",\n");
            } else {
                methods.append("\n");
            }
        }
        methods.append("        );\n");
        methods.append("    }\n\n");

        // Generate toString method
        methods.append("    @Override\n");
        methods.append("    public String toString() {\n");
        methods.append("        return \"").append(className).append("{\" +\n");
        for (int i = 0; i < allFields.length; i++) {
            methods.append("                \"").append(allFields[i]).append("=\" + ").append(allFields[i]);
            if (i < allFields.length - 1) {
                methods.append(" + \",\" +\n");
            } else {
                methods.append(" +\n");
            }
        }
        methods.append("                '}';\n");
        methods.append("    }\n");
        
        return methods.toString();
    }

    /**
     * Generates import statements based on field types.
     * 
     * <p>This method analyzes the field types and generates appropriate import statements
     * for collections, entities, and other non-java.lang types.</p>
     * 
     * @param out the PrintWriter to write the imports to
     * @param fieldInfoMap map of field names to their type information
     */
    private void generateImports(PrintWriter out, Map<String, FieldInfo> fieldInfoMap) {
        // Standard imports that are always needed
        out.println("import com.fasterxml.jackson.annotation.JsonProperty;");
        out.println("import com.fasterxml.jackson.databind.annotation.JsonSerialize;");
        out.println("import com.fasterxml.jackson.databind.ser.std.StdSerializer;");
        out.println("import java.io.Serializable;");
        out.println("import java.util.Objects;");
        
        // Collect imports based on field types
        Set<String> imports = new HashSet<>();
        for (FieldInfo fieldInfo : fieldInfoMap.values()) {
            // Add collection imports (only the base collection type, not with generics)
            if (fieldInfo.isCollection) {
                imports.add("import java.util." + fieldInfo.typeName + ";");
            }
            // Add entity imports for non-java.lang types (only if it's not a collection element type)
            if (fieldInfo.needsImport && !fieldInfo.fullTypeName.startsWith("java.lang.") && !fieldInfo.isCollection) {
                imports.add("import " + fieldInfo.fullTypeName + ";");
            }
        }
        
        // Write all collected imports
        for (String importStatement : imports) {
            out.println(importStatement);
        }
        out.println();
    }

    /**
     * Generates field declarations with proper types and Jackson annotations.
     * 
     * <p>This method generates field declarations for both simple and serialized fields,
     * including proper generic types for collections and Jackson serialization annotations.</p>
     * 
     * @param out the PrintWriter to write the fields to
     * @param simpleFields array of simple field names
     * @param serializedFields array of serialized field names
     * @param serializers array of serializer classes
     * @param fieldInfoMap map of field names to their type information
     */
    private void generateFields(PrintWriter out, String[] simpleFields, String[] serializedFields, 
                               String[] serializers, Map<String, FieldInfo> fieldInfoMap) {
        // Generate simple fields (no special serialization)
        for (String fieldName : simpleFields) {
            FieldInfo fieldInfo = fieldInfoMap.getOrDefault(fieldName, new FieldInfo("Object", "java.lang.Object", false, null, false, null, null, false, false));
            out.println("    @JsonProperty(\"" + fieldName + "\")");
            
            // Add content serialization for collections
            if (fieldInfo.isCollection) {
                out.println("    @JsonSerialize(contentUsing = StdSerializer.class)");
                if (fieldInfo.isMap) {
                    out.println("    private " + fieldInfo.typeName + "<" + fieldInfo.mapKeyType + ", " + fieldInfo.mapValueType + "> " + fieldName + ";");
                } else {
                    out.println("    private " + fieldInfo.typeName + "<" + fieldInfo.collectionElementType + "> " + fieldName + ";");
                }
            } else {
                out.println("    private " + fieldInfo.typeName + " " + fieldName + ";");
            }
            out.println();
        }

        // Generate serialized fields (with custom serializers)
        for (int i = 0; i < serializedFields.length && i < serializers.length; i++) {
            String fieldName = serializedFields[i];
            String serializer = serializers[i];
            FieldInfo fieldInfo = fieldInfoMap.getOrDefault(fieldName, new FieldInfo("Object", "java.lang.Object", false, null, false, null, null, false, false));
            
            out.println("    @JsonProperty(\"" + fieldName + "\")");
            if (fieldInfo.isCollection) {
                // For collections, add both field and content serialization
                out.println("    @JsonSerialize(using = " + serializer + ".class)");
                out.println("    @JsonSerialize(contentUsing = StdSerializer.class)");
                if (fieldInfo.isMap) {
                    out.println("    private " + fieldInfo.typeName + "<" + fieldInfo.mapKeyType + ", " + fieldInfo.mapValueType + "> " + fieldName + ";");
                } else {
                    out.println("    private " + fieldInfo.typeName + "<" + fieldInfo.collectionElementType + "> " + fieldName + ";");
                }
            } else {
                // For simple types, add only field serialization
                out.println("    @JsonSerialize(using = " + serializer + ".class)");
                out.println("    private " + fieldInfo.typeName + " " + fieldName + ";");
            }
            out.println();
        }
    }

    /**
     * Generates constructors for the DTO class.
     * 
     * <p>This method generates both a default constructor and an all-args constructor
     * with proper parameter types including generics for collections.</p>
     * 
     * @param out the PrintWriter to write the constructors to
     * @param className the name of the DTO class
     * @param simpleFields array of simple field names
     * @param serializedFields array of serialized field names
     * @param fieldInfoMap map of field names to their type information
     */
    private void generateConstructors(PrintWriter out, String className, String[] simpleFields, 
                                     String[] serializedFields, Map<String, FieldInfo> fieldInfoMap) {
        // Generate default constructor
        out.println("    public " + className + "() {");
        out.println("    }");
        out.println();

        // Generate all-args constructor
        out.println("    public " + className + "(");
        
        // Add simple field parameters
        for (int i = 0; i < simpleFields.length; i++) {
            String fieldName = simpleFields[i];
            FieldInfo fieldInfo = fieldInfoMap.getOrDefault(fieldName, new FieldInfo("Object", "java.lang.Object", false, null, false, null, null, false, false));
            String paramType;
            if (fieldInfo.isCollection) {
                if (fieldInfo.isMap) {
                    paramType = fieldInfo.typeName + "<" + fieldInfo.mapKeyType + ", " + fieldInfo.mapValueType + ">";
                } else {
                    paramType = fieldInfo.typeName + "<" + fieldInfo.collectionElementType + ">";
                }
            } else {
                paramType = fieldInfo.typeName;
            }
            out.print("        " + paramType + " " + fieldName);
            if (i < simpleFields.length - 1 || serializedFields.length > 0) {
                out.println(",");
            } else {
                out.println();
            }
        }
        
        // Add serialized field parameters
        for (int i = 0; i < serializedFields.length; i++) {
            String fieldName = serializedFields[i];
            FieldInfo fieldInfo = fieldInfoMap.getOrDefault(fieldName, new FieldInfo("Object", "java.lang.Object", false, null, false, null, null, false, false));
            String paramType;
            if (fieldInfo.isCollection) {
                if (fieldInfo.isMap) {
                    paramType = fieldInfo.typeName + "<" + fieldInfo.mapKeyType + ", " + fieldInfo.mapValueType + ">";
                } else {
                    paramType = fieldInfo.typeName + "<" + fieldInfo.collectionElementType + ">";
                }
            } else {
                paramType = fieldInfo.typeName;
            }
            out.print("        " + paramType + " " + fieldName);
            if (i < serializedFields.length - 1) {
                out.println(",");
            } else {
                out.println();
            }
        }
        
        // Constructor body - assign parameters to fields
        out.println("    ) {");
        for (String fieldName : simpleFields) {
            out.println("        this." + fieldName + " = " + fieldName + ";");
        }
        for (String fieldName : serializedFields) {
            out.println("        this." + fieldName + " = " + fieldName + ";");
        }
        out.println("    }");
        out.println();
    }

    /**
     * Generates getter and setter methods for all fields.
     * 
     * <p>This method generates getters and setters for both simple and serialized fields,
     * with proper return types including generics for collections.</p>
     * 
     * @param out the PrintWriter to write the methods to
     * @param simpleFields array of simple field names
     * @param serializedFields array of serialized field names
     * @param fieldInfoMap map of field names to their type information
     */
    private void generateGettersAndSetters(PrintWriter out, String[] simpleFields, String[] serializedFields, 
                                          Map<String, FieldInfo> fieldInfoMap) {
                // Generate getters and setters for simple fields
        for (String fieldName : simpleFields) {
            generateGetterAndSetter(out, fieldName, fieldInfoMap.getOrDefault(fieldName, new FieldInfo("Object", "java.lang.Object", false, null, false, null, null, false, false)));
        }
        
        // Generate getters and setters for serialized fields
        for (String fieldName : serializedFields) {
            generateGetterAndSetter(out, fieldName, fieldInfoMap.getOrDefault(fieldName, new FieldInfo("Object", "java.lang.Object", false, null, false, null, null, false, false)));
        }
    }

    /**
     * Generates a getter and setter method for a single field.
     * 
     * <p>This method generates both getter and setter methods with proper types,
     * including generic types for collections.</p>
     * 
     * @param out the PrintWriter to write the methods to
     * @param fieldName the name of the field
     * @param fieldInfo the type information for the field
     */
    private void generateGetterAndSetter(PrintWriter out, String fieldName, FieldInfo fieldInfo) {
        String capitalizedFieldName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        String returnType;
        if (fieldInfo.isCollection) {
            if (fieldInfo.isMap) {
                returnType = fieldInfo.typeName + "<" + fieldInfo.mapKeyType + ", " + fieldInfo.mapValueType + ">";
            } else {
                returnType = fieldInfo.typeName + "<" + fieldInfo.collectionElementType + ">";
            }
        } else {
            returnType = fieldInfo.typeName;
        }
        
        // Generate getter method
        out.println("    public " + returnType + " get" + capitalizedFieldName + "() {");
        out.println("        return " + fieldName + ";");
        out.println("    }");
        out.println();

        // Generate setter method
        out.println("    public void set" + capitalizedFieldName + "(" + returnType + " " + fieldName + ") {");
        out.println("        this." + fieldName + " = " + fieldName + ";");
        out.println("    }");
        out.println();
    }

    /**
     * Generates utility methods (equals, hashCode, toString) for the DTO class.
     * 
     * <p>These methods are generated based on all fields (both simple and serialized)
     * to ensure proper object comparison and string representation.</p>
     * 
     * @param out the PrintWriter to write the methods to
     * @param className the name of the DTO class
     * @param simpleFields array of simple field names
     * @param serializedFields array of serialized field names
     */
    private void generateUtilityMethods(PrintWriter out, String className, String[] simpleFields, String[] serializedFields) {
        // Generate equals method
        out.println("    @Override");
        out.println("    public boolean equals(Object obj) {");
        out.println("        if (this == obj) return true;");
        out.println("        if (obj == null || getClass() != obj.getClass()) return false;");
        out.println("        " + className + " that = (" + className + ") obj;");
        out.println("        return ");
        
        // Combine all fields for comparison
        String[] allFields = new String[simpleFields.length + serializedFields.length];
        System.arraycopy(simpleFields, 0, allFields, 0, simpleFields.length);
        System.arraycopy(serializedFields, 0, allFields, simpleFields.length, serializedFields.length);
        
        // Generate field comparisons
        for (int i = 0; i < allFields.length; i++) {
            out.print("                Objects.equals(" + allFields[i] + ", that." + allFields[i] + ")");
            if (i < allFields.length - 1) {
                out.println(" &&");
            } else {
                out.println(";");
            }
        }
        out.println("    }");
        out.println();

        // Generate hashCode method
        out.println("    @Override");
        out.println("    public int hashCode() {");
        out.println("        return Objects.hash(");
        for (int i = 0; i < allFields.length; i++) {
            out.print("            " + allFields[i]);
            if (i < allFields.length - 1) {
                out.println(",");
            } else {
                out.println();
            }
        }
        out.println("        );");
        out.println("    }");
        out.println();

        // Generate toString method
        out.println("    @Override");
        out.println("    public String toString() {");
        out.println("        return \"" + className + "{\" +");
        for (int i = 0; i < allFields.length; i++) {
            out.print("                \"" + allFields[i] + "=\" + " + allFields[i]);
            if (i < allFields.length - 1) {
                out.println(" + \",\" +");
            } else {
                out.println(" +");
            }
        }
        out.println("                '}';");
        out.println("    }");
    }

    /**
     * Gets the package name from a class element.
     * 
     * <p>This method extracts the package name from the class's enclosing element.
     * If the class is not in a package, it returns an empty string.</p>
     * 
     * @param classElement the class element
     * @return the package name, or empty string if not in a package
     */
    private String getPackageName(TypeElement classElement) {
        Element enclosingElement = classElement.getEnclosingElement();
        if (enclosingElement.getKind() == ElementKind.PACKAGE) {
            return ((PackageElement) enclosingElement).getQualifiedName().toString();
        }
        return "";
    }

    /**
     * Generates the DTO package name based on the source package.
     *
     * <p>This method creates a subpackage called "autogendto" within the source package
     * where the generated DTO classes will be placed.</p>
     * 
     * @param sourcePackageName the source package name
     * @return the DTO package name (source package + .autogendto)
     */
    private String getDTOPackageName(String sourcePackageName) {
        if (sourcePackageName == null || sourcePackageName.isEmpty()) {
            throw new RuntimeException("Cannot get package name to store auto gen files in");
        }

        // Create a subpackage called "autogendto" within the source package
        return sourcePackageName + ".autogendto";
    }

    /**
     * Finds the source directory by analyzing the location of the annotated class.
     * This method works with any project structure by dynamically detecting the source root.
     * 
     * @param sourceClass the annotated class element
     * @param targetPackageName the target package name for the DTO
     * @return the source directory path
     */
    private String findSourceDirectoryFromClass(TypeElement sourceClass, String targetPackageName) {
        try {
            // Get the source file URI from the class element
            String sourceFileUri = sourceClass.getEnclosingElement().toString();

            // Try to get the actual file path from the annotation processing environment
            // This is a more reliable way to get the source file location
            String sourceClassName = sourceClass.getQualifiedName().toString();
            messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING , "src file uri = " + sourceFileUri);
            messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING , "src file full name = " + sourceClassName);

            String sourcePackageName = getPackageName(sourceClass);

            // Get the current working directory
            String currentDir = System.getProperty("user.dir");

            // For DTO packages, we need to find the parent package directory
            String parentPackageName = targetPackageName;
            if (targetPackageName.endsWith(".autogendto")) {
                parentPackageName = targetPackageName.substring(0, targetPackageName.length() - ".autogendto".length());
            }
            
            // Convert package names to directory paths
            String sourcePackagePath = sourcePackageName.replace('.', '/');
            String parentPackagePath = parentPackageName.replace('.', '/');
            
            // Search for the source directory by looking for the source package
            // Prioritize common module names that might contain the source
            String[] possibleSourceDirs = {
                currentDir + "/Main/src/main/java",      // Prioritize Main module
                currentDir + "/app/src/main/java",
                currentDir + "/core/src/main/java",
                currentDir + "/api/src/main/java",
                currentDir + "/service/src/main/java",
                currentDir + "/web/src/main/java",
                currentDir + "/client/src/main/java",
                currentDir + "/server/src/main/java",
                currentDir + "/src/main/java",           // Fallback to root src
                currentDir + "/src/test/java"
            };
            
            // First, try to find the directory containing the source class
            for (String sourceDir : possibleSourceDirs) {
                File sourcePackageDir = new File(sourceDir + "/" + sourcePackagePath);
                
                if (sourcePackageDir.exists() && sourcePackageDir.isDirectory()) {
                    // Check if this directory contains the source class file
                    File[] files = sourcePackageDir.listFiles();
                    if (files != null) {
                        boolean hasOriginalSourceFiles = false;
                        for (File file : files) {
                            if (file.isFile() && file.getName().endsWith(".java") && !file.getName().endsWith("DTO.java")) {
                                hasOriginalSourceFiles = true;
                                break;
                            }
                        }
                        if (hasOriginalSourceFiles) {
                            // Found the source directory with original source files
                            return sourceDir;
                        }
                    }
                }
            }
            
            // If not found, try to find any directory that contains the parent package
            for (String sourceDir : possibleSourceDirs) {
                File parentPackageDir = new File(sourceDir + "/" + parentPackagePath);
                
                if (parentPackageDir.exists() && parentPackageDir.isDirectory()) {
                    // Check if this directory contains original source files (not just generated DTOs)
                    File[] files = parentPackageDir.listFiles();
                    if (files != null) {
                        boolean hasOriginalSourceFiles = false;
                        for (File file : files) {
                            if (file.isFile() && file.getName().endsWith(".java") && !file.getName().endsWith("DTO.java")) {
                                hasOriginalSourceFiles = true;
                                break;
                            }
                        }
                        if (hasOriginalSourceFiles) {
                            return sourceDir;
                        }
                    }
                }
            }
            
            // Fallback: search recursively for src/main/java directories
            File current = new File(currentDir);
            while (current != null) {
                File srcMainJava = new File(current, "src/main/java");
                if (srcMainJava.exists() && srcMainJava.isDirectory()) {
                    return srcMainJava.getAbsolutePath();
                }
                current = current.getParentFile();
            }
            
            // Final fallback
            return currentDir + "/src/main/java";
            
        } catch (Exception e) {
            messager.printMessage(Diagnostic.Kind.WARNING, 
                "Error detecting source directory: " + e.getMessage());
            return System.getProperty("user.dir") + "/src/main/java";
        }
    }
}
