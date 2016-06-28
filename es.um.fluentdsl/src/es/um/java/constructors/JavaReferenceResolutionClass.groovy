package es.um.java.constructors

import java.lang.reflect.Method;

class JavaReferenceResolutionClass {
	
	String builderOptions
	String referencesResolution
	String reference
	String referenceMultiple
	
	JavaReferenceResolutionClass(String packageName) {
		builderOptions = 
		/package ${packageName}.builders;
		|
		|import java.util.HashMap;
		|import java.util.Map;
		|
		|public class BuilderOptions {
		|
		|	private static BuilderOptions instance;
		|	
		|	private static final String FIRST_DEFAULT_VALUE = "id";
		|	private static final String SECOND_DEFAULT_VALUE = "name";
		|	
		|	private Map<String, String> identifierFieldName;
		|	
		|	public static BuilderOptions getInstance() {
		|		if (instance == null) {
		|			instance = new BuilderOptions();
		|		}
		|		return instance;
		|	}
		|	
		|	private BuilderOptions() {
		|		identifierFieldName = new HashMap<String, String>();
		|	}
		|	
		|	public void setIdentifierField(String entity, String field) {
		|		identifierFieldName.put(entity, field);
		|	}
		|	
		|	String getIdentifierField(String entity) {
		|		return identifierFieldName.get(entity);
		|	}
		|	
		|	public void purge() {
		|		identifierFieldName.clear();
		|	}
		|	
		|	public String getFirstDefaultValue() {
		|		return FIRST_DEFAULT_VALUE;
		|	}
		|	
		|	public String getSecondDefaultValue() {
		|		return SECOND_DEFAULT_VALUE;
		|	}
		|	
		|	
		|}/.stripMargin()
		
		referenceMultiple =
		/package ${packageName}.builders;
		|
		|import java.lang.reflect.Method;
		|
		|import org.eclipse.emf.common.util.EList;
		|import org.eclipse.emf.ecore.EObject;
		|
		|public class ReferenceMultiple extends Reference {
		|	
		|	public ReferenceMultiple(String from, String to, String methodName) {
		|		super(from, to, methodName);
		|	}
		|	
		|	@Override
		|	public void execute() {
		|		EObject fromObject = referencesResolution.getEObject(from);
		|		EObject toObject = referencesResolution.getEObject(to);
		|
		|		Class<? extends EObject> fromClass = fromObject.getClass();
		|		
		|		try {
		|			Method method = fromClass.getMethod(methodName);
		|			EList eList = (EList) method.invoke(fromObject);
		|			method = eList.getClass().getMethod("add", new Class[]{Object.class});
		|			method.invoke(eList, toObject);
		|		} catch (Exception e) {
		|			e.printStackTrace();
		|		}		
		|	}
		|	
		}/.stripMargin()
		
		reference = 
		/package ${packageName}.builders;
		|
		|import java.lang.reflect.Method;
		|
		|import org.eclipse.emf.ecore.EObject;
		|
		|public class Reference {
		|	
		|	protected String from;
		|	protected String to;
		|	protected String methodName;
		|	protected ReferencesResolution referencesResolution;
		|	
		|	public Reference(String from, String to, String methodName) {
		|		this.from = from;
		|		this.to = to;
		|		this.methodName = methodName;
		|		this.referencesResolution = ReferencesResolution.getInstance();
		|	}
		|	
		|	public void execute() {
		|		EObject fromObject = referencesResolution.getEObject(from);
		|		EObject toObject = referencesResolution.getEObject(to);
		|
		|		Class<? extends EObject> fromClass = fromObject.getClass();
		|		Class<? extends EObject> toClass = toObject.getClass();
		|		
		|		try {
		|			Method method = fromClass.getMethod(methodName, new Class[]{toClass.getInterfaces()[0]});
		|			method.invoke(fromObject, toObject);
		|		} catch (Exception e) {
		|			e.printStackTrace();
		|		}		
		|	}
		|	
		|}/.stripMargin()
		
		referencesResolution =
		/package ${packageName}.builders;
		|
		|import java.lang.reflect.Field;
		|import java.lang.reflect.Method;
		|import java.util.HashMap;
		|import java.util.LinkedList;
		|import java.util.List;
		|import java.util.Map;
		|
		|import org.eclipse.emf.ecore.EObject;
		|
		|public class ReferencesResolution {
		|	
		|	private static ReferencesResolution referenceResolution;
		|
		|	private List<Reference> referencesList; 
		|	private Map<String, EObject> identifiersMap; 
		|	
		|	public static ReferencesResolution getInstance() {
		|		if (referenceResolution == null) {
		|			referenceResolution = new ReferencesResolution();
		|		}
		|		
		|		return referenceResolution;
		|	}
		|	
		|	private ReferencesResolution() {
		|		referencesList = new LinkedList<Reference>();
		|		identifiersMap = new HashMap<String, EObject>();
		|	}
		|	
		|	public void putReference(String from, String methodName, String to) {
		|		referencesList.add(new Reference(from, to, methodName));
		|	}
		|	
		|	public void putMultipleReference(String from, String methodName, String ...toArray) {
		|		for (String to : toArray) {
		|			referencesList.add(new ReferenceMultiple(from, to, methodName));
		|		}
		|		
		|	}
		|	
		|	private String generateGetMethodName(String identifier) {
		|		String methodName = "get" 
		|				+ Character.toUpperCase(identifier.charAt(0)) 
		|				+ identifier.substring(1, identifier.length());
		|		
		|		return methodName;
		|	}
		|	
		|	private <T> boolean putFieldAsIdentifier(T builder, EObject object, String fieldName, String className) {
		|		String methodName = generateGetMethodName(fieldName);		
		|			
		|		try {
		|			Method method = builder.getClass().getDeclaredMethod(methodName);
		|			String id = (String) method.invoke(builder);
		|			
		|			if (id == null || id.equals("")) {
		|				throw new RuntimeException("Identifier '" + fieldName + "' has not been set for " + className);
		|			} else if (identifiersMap.containsKey(id)) {
		|				throw new RuntimeException("Duplicate identifier: " + id);
		|			}
		|			
		|			identifiersMap.put(id, object);
		|			return false;
		|		} catch (Exception e) {
		|			e.printStackTrace();
		|		}		
		|			
		|		return true;
		|	}
		|	
		|	public <T> String putIdentifier(T builder, String className, EObject object) {
		|		
		|		BuilderOptions instance = BuilderOptions.getInstance();
		|		
		|		boolean tryDefaults = trySetSpecificField(builder, className, object, instance);		
		|		if (tryDefaults) {
		|			tryDefaultsFields(builder, object, className, instance);
		|			return serachIdentifier(builder, className, instance);
		|		} else {
		|			return null;
		|		}
		|	}
		|
		|	private <T> boolean trySetSpecificField(T builder, String className, EObject object, BuilderOptions instance) {
		|		String identifierField = instance.getIdentifierField(className);
		|		
		|		if (identifierField != null) {
		|			return putFieldAsIdentifier(builder, object, identifierField, className);
		|		} else {
		|			return true;
		|		}
		|	}
		|
		|	private <T> boolean fieldExits(T builder, String fieldName) {
		|		Field[] fields = builder.getClass().getDeclaredFields();
		|		
		|		for (Field field : fields) {
		|			String name = field.getName();
		|			if (name.equals(fieldName)) {
		|				return true;
		|			}
		|		}
		|		
		|		return false;
		|	}
		|	
		|	private <T> String serachIdentifier(T builder, String className, BuilderOptions instance) {
		|		String fieldName = instance.getIdentifierField(className);
		|		boolean fieldExits = true;
		|		
		|		if (fieldName == null) {
		|			fieldName = instance.getFirstDefaultValue();
		|			fieldExits = fieldExits(builder, fieldName);
		|		}
		|		
		|		if (!fieldExits) {
		|			fieldName = instance.getSecondDefaultValue();
		|			fieldExits = fieldExits(builder, fieldName);
		|		}
		|		
		|		String methodName = generateGetMethodName(fieldName);		
		|		try {
		|			Method method = builder.getClass().getDeclaredMethod(methodName);
		|			return (String) method.invoke(builder);
		|		} catch (Exception e) {
		|			e.printStackTrace();
		|		}
		|		return null;
		|	}
		|	
		|	private <T> void tryDefaultsFields(T builder, EObject object, String className, BuilderOptions instance) {
		|		String fieldName = instance.getFirstDefaultValue();
		|		boolean fieldExits = fieldExits(builder, fieldName);
		|		
		|		if (!fieldExits) {
		|			fieldName = instance.getSecondDefaultValue();
		|			fieldExits = fieldExits(builder, fieldName);
		|		}
		|				
		|		if (!fieldExits) {
		|			String identifierField = instance.getIdentifierField(className);	
		|			throw new RuntimeException("No field as Identifier found for " + className + " Class: "
		|						+ (identifierField != null ? "\nSpecific field:" + identifierField: "") 
		|						+ "\nDefault fields: id, name");
		|		}
		|		
		|		putFieldAsIdentifier(builder, object, fieldName, className);
		|	}
		|	
		|	public EObject getEObject(String identifier) {		
		|		EObject eObject = identifiersMap.get(identifier);
		|		
		|		if (eObject == null) {
		|			throw new RuntimeException("Identifiers not found: " + identifier);
		|		}
		|		
		|		return eObject;
		|	}
		|	
		|	public void execute() {
		|		for (Reference reference : referencesList) {
		|			reference.execute();
		|		}
		|	}
		|	
		|	public void purge() {
		|		referencesList.clear();
		|		identifiersMap.clear();
		|	}
		|	
		|}/.stripMargin()
	}
	
}
