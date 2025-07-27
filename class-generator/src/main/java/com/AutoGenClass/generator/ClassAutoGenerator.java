package com.AutoGenClass.generator;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
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
            System.out.println("It's over!!!");
            return false;
        }

        System.out.println("total annotations here : ");
        for (TypeElement typeElement : annotations){
            System.out.println("element = " + typeElement);
            System.out.println("Enclosing Element = " + typeElement.getEnclosingElement());
            System.out.println("Qualified Name = " + typeElement.getQualifiedName());
            System.out.println("Simple Name = " + typeElement.getSimpleName());
            System.out.println("============================================================");
        }

        // Process each annotation type
        for (TypeElement annotation : annotations) {
            Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);
            System.out.println("Elements annotated with " + annotation.getSimpleName());
            for (Element element : annotatedElements){
                System.out.println("element = " + element);
                System.out.println("Enclosing Element = " + element.getEnclosingElement());
                System.out.println("Simple Name = " + element.getSimpleName());
                System.out.println("Annotation = " + element.getAnnotation(AutoGen.class));
                System.out.println("============================================================");
            }
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
        System.out.println("processing class : " + classElement);
        AutoGen autoGen = classElement.getAnnotation(AutoGen.class);
        if (autoGen == null) {
            System.out.println("class : " + classElement + " is annotated but not with autogen");
            return;
        }

        // Extract annotation parameters
        String className = autoGen.name();
        String packageName = getPackageName(classElement);
        String[] simpleFields = autoGen.simpleFields();
        String[] serializedFields = autoGen.serializedFields();
        Class<?>[] serializers = autoGen.serializers();
        
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
         * @param isEntity whether this is an entity type
         * @param needsImport whether this type needs an import
         */
        FieldInfo(String typeName, String fullTypeName, boolean isCollection, String collectionElementType, boolean isEntity, boolean needsImport) {
            this.typeName = typeName;
            this.fullTypeName = fullTypeName;
            this.isCollection = isCollection;
            this.collectionElementType = collectionElementType;
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
                fieldInfoMap.put(fieldName, new FieldInfo("Object", "java.lang.Object", false, null, false, false));
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
            return new FieldInfo(getPrimitiveTypeName(typeName), fullTypeName, false, null, false, false);
        }
        
        // Handle wrapper types (Integer, Long, String, etc.)
        if (isWrapperType(typeName)) {
            return new FieldInfo(getWrapperTypeName(typeName), fullTypeName, false, null, false, false);
        }
        
        // Handle collection types (List, Set, Map, etc.)
        if (isCollectionType(typeName)) {
            String elementType = extractCollectionElementType(typeName);
            String simpleElementType = getSimpleTypeName(elementType);
            return new FieldInfo(getCollectionTypeName(typeName), fullTypeName, true, simpleElementType, false, true);
        }
        
        // Handle arrays (String[], User[], etc.)
        if (typeName.endsWith("[]")) {
            String elementType = typeName.substring(0, typeName.length() - 2);
            String simpleElementType = getSimpleTypeName(elementType);
            return new FieldInfo(simpleElementType + "[]", fullTypeName, true, simpleElementType, false, true);
        }
        
        // Handle other types (potentially entities from external JARs)
        String simpleTypeName = getSimpleTypeName(typeName);
        boolean isEntity = !isJavaLangType(typeName);
        boolean needsImport = isEntity && !typeName.contains("java.lang.");
        
        return new FieldInfo(simpleTypeName, fullTypeName, false, null, isEntity, needsImport);
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
                                 String[] simpleFields, String[] serializedFields, Class<?>[] serializers,
                                 Map<String, FieldInfo> fieldInfoMap) 
            throws IOException {
        
        // Create the source file
        JavaFileObject sourceFile = filer.createSourceFile(packageName + "." + className);
        
        try (PrintWriter out = new PrintWriter(sourceFile.openWriter())) {
            // Generate package declaration
            if (!packageName.isEmpty()) {
                out.println("package " + packageName + ";");
                out.println();
            }

            // Generate imports based on field types
            generateImports(out, fieldInfoMap);

            // Generate class documentation and declaration
            out.println("/**");
            out.println(" * Auto-generated DTO class for " + sourceClass.getQualifiedName());
            out.println(" */");
            out.println("public class " + className + " implements Serializable {");
            out.println("    private static final long serialVersionUID = 1L;");
            out.println();

            // Generate fields with proper types and annotations
            generateFields(out, simpleFields, serializedFields, serializers, fieldInfoMap);

            // Generate constructors
            generateConstructors(out, className, simpleFields, serializedFields, fieldInfoMap);

            // Generate getters and setters
            generateGettersAndSetters(out, simpleFields, serializedFields, fieldInfoMap);

            // Generate utility methods (equals, hashCode, toString)
            generateUtilityMethods(out, className, simpleFields, serializedFields);

            out.println("}");
        }
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
            // Add collection imports
            if (fieldInfo.isCollection) {
                imports.add("import java.util." + fieldInfo.typeName + ";");
            }
            // Add entity imports for non-java.lang types
            if (fieldInfo.needsImport && !fieldInfo.fullTypeName.startsWith("java.lang.")) {
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
                               Class<?>[] serializers, Map<String, FieldInfo> fieldInfoMap) {
        // Generate simple fields (no special serialization)
        for (String fieldName : simpleFields) {
            FieldInfo fieldInfo = fieldInfoMap.getOrDefault(fieldName, new FieldInfo("Object", "java.lang.Object", false, null, false, false));
            out.println("    @JsonProperty(\"" + fieldName + "\")");
            
            // Add content serialization for collections
            if (fieldInfo.isCollection) {
                out.println("    @JsonSerialize(contentUsing = StdSerializer.class)");
                out.println("    private " + fieldInfo.typeName + "<" + fieldInfo.collectionElementType + "> " + fieldName + ";");
            } else {
                out.println("    private " + fieldInfo.typeName + " " + fieldName + ";");
            }
            out.println();
        }

        // Generate serialized fields (with custom serializers)
        for (int i = 0; i < serializedFields.length && i < serializers.length; i++) {
            String fieldName = serializedFields[i];
            Class<?> serializer = serializers[i];
            FieldInfo fieldInfo = fieldInfoMap.getOrDefault(fieldName, new FieldInfo("Object", "java.lang.Object", false, null, false, false));
            
            out.println("    @JsonProperty(\"" + fieldName + "\")");
            if (fieldInfo.isCollection) {
                // For collections, add both field and content serialization
                out.println("    @JsonSerialize(using = " + serializer.getCanonicalName() + ".class)");
                out.println("    @JsonSerialize(contentUsing = StdSerializer.class)");
                out.println("    private " + fieldInfo.typeName + "<" + fieldInfo.collectionElementType + "> " + fieldName + ";");
            } else {
                // For simple types, add only field serialization
                out.println("    @JsonSerialize(using = " + serializer.getCanonicalName() + ".class)");
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
            FieldInfo fieldInfo = fieldInfoMap.getOrDefault(fieldName, new FieldInfo("Object", "java.lang.Object", false, null, false, false));
            String paramType = fieldInfo.isCollection ? fieldInfo.typeName + "<" + fieldInfo.collectionElementType + ">" : fieldInfo.typeName;
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
            FieldInfo fieldInfo = fieldInfoMap.getOrDefault(fieldName, new FieldInfo("Object", "java.lang.Object", false, null, false, false));
            String paramType = fieldInfo.isCollection ? fieldInfo.typeName + "<" + fieldInfo.collectionElementType + ">" : fieldInfo.typeName;
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
            generateGetterAndSetter(out, fieldName, fieldInfoMap.getOrDefault(fieldName, new FieldInfo("Object", "java.lang.Object", false, null, false, false)));
        }

        // Generate getters and setters for serialized fields
        for (String fieldName : serializedFields) {
            generateGetterAndSetter(out, fieldName, fieldInfoMap.getOrDefault(fieldName, new FieldInfo("Object", "java.lang.Object", false, null, false, false)));
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
        String returnType = fieldInfo.isCollection ? fieldInfo.typeName + "<" + fieldInfo.collectionElementType + ">" : fieldInfo.typeName;
        
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
}
