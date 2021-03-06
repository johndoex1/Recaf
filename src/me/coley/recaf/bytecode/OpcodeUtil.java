package me.coley.recaf.bytecode;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

import me.coley.recaf.Logging;
import me.coley.recaf.bytecode.insn.*;
import me.coley.recaf.util.Reflect;

public class OpcodeUtil implements Opcodes {
	private static final Map<Integer, Integer> opcodeToType = new LinkedHashMap<>();
	private static final Map<Integer, String> opcodeToName = new LinkedHashMap<>();
	private static final Map<String, Integer> nameToOpcode = new LinkedHashMap<>();
	private static final Map<Integer, String> frameToName = new LinkedHashMap<>();
	private static final Map<String, Integer> nameToFrame = new LinkedHashMap<>();
	private static final Map<Integer, String> tagToName = new LinkedHashMap<>();
	private static final Map<String, Integer> nameToTag = new LinkedHashMap<>();
	private static final Map<Integer, Set<String>> insnTypeToCodes = new LinkedHashMap<>();
	/**
	 * Opcodes of INSN type.
	 */
	public static final Set<String> OPS_INSN = Stream.of("AALOAD", "AASTORE", "ACONST_NULL", "ARETURN", "ARRAYLENGTH", "ATHROW",
			"BALOAD", "BASTORE", "CALOAD", "CASTORE", "D2F", "D2I", "D2L", "DADD", "DALOAD", "DASTORE", "DCMPG", "DCMPL",
			"DCONST_0", "DCONST_1", "DDIV", "DMUL", "DNEG", "DREM", "DRETURN", "DSUB", "DUP", "DUP2", "DUP2_X1", "DUP2_X2",
			"DUP_X1", "DUP_X2", "F2D", "F2I", "F2L", "FADD", "FALOAD", "FASTORE", "FCMPG", "FCMPL", "FCONST_0", "FCONST_1",
			"FCONST_2", "FDIV", "FMUL", "FNEG", "FREM", "FRETURN", "FSUB", "I2B", "I2C", "I2D", "I2F", "I2L", "I2S", "IADD",
			"IALOAD", "IAND", "IASTORE", "ICONST_0", "ICONST_1", "ICONST_2", "ICONST_3", "ICONST_4", "ICONST_5", "ICONST_M1",
			"IDIV", "IMUL", "INEG", "IOR", "IREM", "IRETURN", "ISHL", "ISHR", "ISUB", "IUSHR", "IXOR", "L2D", "L2F", "L2I",
			"LADD", "LALOAD", "LAND", "LASTORE", "LCMP", "LCONST_0", "LCONST_1", "LDIV", "LMUL", "LNEG", "LOR", "LREM", "LRETURN",
			"LSHL", "LSHR", "LSUB", "LUSHR", "LXOR", "MONITORENTER", "MONITOREXIT", "NOP", "POP", "POP2", "RETURN", "SALOAD",
			"SASTORE", "SWAP").collect(Collectors.toCollection(LinkedHashSet::new));
	/**
	 * Subset of {@link #OPS_INSN} for constants.
	 */
	public static final Set<String> OPS_INSN_SUB_CONSTS = Stream.of("ACONST_NULL", "DCONST_0", "DCONST_1", "FCONST_0", "FCONST_1",
			"FCONST_2", "ICONST_0", "ICONST_1", "ICONST_2", "ICONST_3", "ICONST_4", "ICONST_5", "ICONST_M1", "LCONST_0",
			"LCONST_1").collect(Collectors.toCollection(LinkedHashSet::new));
	/**
	 * Subset of {@link #OPS_INSN} for array loads/saves/etc.
	 */
	public static final Set<String> OPS_INSN_SUB_ARRAY = Stream.of("AALOAD", "AASTORE", "ARRAYLENGTH", "BALOAD", "BASTORE",
			"CALOAD", "CASTORE", "DALOAD", "DASTORE", "FALOAD", "FASTORE", "IALOAD", "IASTORE", "LALOAD", "LASTORE", "SALOAD",
			"SASTORE").collect(Collectors.toCollection(LinkedHashSet::new));
	/**
	 * Subset of {@link #OPS_INSN} for stack management.
	 */
	public static final Set<String> OPS_INSN_SUB_STACK = Stream.of("DUP", "DUP2", "DUP2_X1", "DUP2_X2", "DUP_X1", "DUP_X2", "POP",
			"POP2", "SWAP").collect(Collectors.toCollection(LinkedHashSet::new));
	/**
	 * Subset of {@link #OPS_INSN} for math handling.
	 */
	public static final Set<String> OPS_INSN_SUB_MATH = Stream.of("DADD", "DDIV", "DMUL", "DNEG", "DREM", "DSUB", "FADD", "FDIV",
			"FMUL", "FNEG", "FREM", "FSUB", "IADD", "IAND", "IDIV", "IMUL", "INEG", "IOR", "IREM", "ISHL", "ISHR", "ISUB",
			"IUSHR", "IXOR", "LADD", "LAND", "LDIV", "LMUL", "LNEG", "LOR", "LREM", "LSHL", "LSHR", "LSUB", "LUSHR").collect(
					Collectors.toCollection(LinkedHashSet::new));
	/**
	 * Subset of {@link #OPS_INSN} for type conversion.
	 */
	public static final Set<String> OPS_INSN_SUB_CONVERT = Stream.of("D2F", "D2I", "D2L", "F2D", "F2I", "F2L", "I2B", "I2C",
			"I2D", "I2F", "I2L", "I2S", "L2D", "L2F", "L2I").collect(Collectors.toCollection(LinkedHashSet::new));
	/**
	 * Subset of {@link #OPS_INSN} for primitve comparisons.
	 */
	public static final Set<String> OPS_INSN_SUB_COMPARE = Stream.of("DCMPG", "DCMPL", "FCMPG", "FCMPL", "LCMP").collect(
			Collectors.toCollection(LinkedHashSet::new));
	/**
	 * Subset of {@link #OPS_INSN} for returns.
	 */
	public static final Set<String> OPS_INSN_SUB_RETURN = Stream.of("ARETURN", "DRETURN", "FRETURN", "IRETURN", "LRETURN",
			"RETURN").collect(Collectors.toCollection(LinkedHashSet::new));
	/**
	 * Subset of {@link #OPS_INSN} for monitors.
	 */
	public static final Set<String> OPS_INSN_SUB_MONITOR = Stream.of("MONITORENTER", "MONITOREXIT").collect(Collectors
			.toCollection(LinkedHashSet::new));
	/**
	 * Subset of {@link #OPS_INSN} for exceptions.
	 */
	public static final Set<String> OPS_INSN_SUB_EXCEPTION = Stream.of("ATHROW").collect(Collectors.toCollection(
			LinkedHashSet::new));
	/**
	 * Opcodes of INT type.
	 */
	public static final Set<String> OPS_INT = Stream.of("BIPUSH", "SIPUSH", "NEWARRAY").collect(Collectors.toCollection(
			LinkedHashSet::new));
	/**
	 * Opcodes of INT type.
	 */
	public static final Set<String> OPS_VAR = Stream.of("ALOAD", "ASTORE", "DLOAD", "DSTORE", "FLOAD", "FSTORE", "ILOAD",
			"ISTORE", "LLOAD", "LSTORE", "RET").collect(Collectors.toCollection(LinkedHashSet::new));
	/**
	 * Opcodes of TYPE type.
	 */
	public static final Set<String> OPS_TYPE = Stream.of("ANEWARRAY", "CHECKCAST", "INSTANCEOF", "NEW").collect(Collectors
			.toCollection(LinkedHashSet::new));
	/**
	 * Opcodes of FIELD type.
	 */
	public static final Set<String> OPS_FIELD = Stream.of("GETSTATIC", "PUTSTATIC", "GETFIELD", "PUTFIELD").collect(Collectors
			.toCollection(LinkedHashSet::new));
	/**
	 * Opcodes of METHOD type.
	 */
	public static final Set<String> OPS_METHOD = Stream.of("INVOKEVIRTUAL", "INVOKESPECIAL", "INVOKESTATIC", "INVOKEINTERFACE")
			.collect(Collectors.toCollection(LinkedHashSet::new));
	/**
	 * Opcodes of INDY_METHOD type.
	 */
	public static final Set<String> OPS_INDY_METHOD = Stream.of("INVOKEDYNAMIC").collect(Collectors.toCollection(
			LinkedHashSet::new));
	/**
	 * Opcodes of JUMP type.
	 */
	public static final Set<String> OPS_JUMP = Stream.of("GOTO", "IF_ACMPEQ", "IF_ACMPNE", "IF_ICMPEQ", "IF_ICMPGE", "IF_ICMPGT",
			"IF_ICMPLE", "IF_ICMPLT", "IF_ICMPNE", "IFEQ", "IFGE", "IFGT", "IFLE", "IFLT", "IFNE", "IFNONNULL", "IFNULL", "JSR")
			.collect(Collectors.toCollection(LinkedHashSet::new));
	/**
	 * Opcodes of LDC type.
	 */
	public static final Set<String> OPS_LDC = Stream.of("LDC").collect(Collectors.toCollection(LinkedHashSet::new));
	/**
	 * Opcodes of IINC type.
	 */
	public static final Set<String> OPS_IINC = Stream.of("IINC").collect(Collectors.toCollection(LinkedHashSet::new));
	/**
	 * Opcodes of TABLESWITCH type.
	 */
	public static final Set<String> OPS_TABLESWITCH = Stream.of("TABLESWITCH").collect(Collectors.toCollection(
			LinkedHashSet::new));
	/**
	 * Opcodes of LOOKUPSWITCH type.
	 */
	public static final Set<String> OPS_LOOKUPSWITCH = Stream.of("LOOKUPSWITCH").collect(Collectors.toCollection(
			LinkedHashSet::new));
	/**
	 * Opcodes of LOOKUPSWITCH type.
	 */
	public static final Set<String> OPS_MULTIANEWARRAY = Stream.of("MULTIANEWARRAY").collect(Collectors.toCollection(
			LinkedHashSet::new));
	/**
	 * Opcodes of FRAME type.
	 */
	public static final Set<String> OPS_FRAME = Stream.of("F_NEW", "F_FULL", "F_APPEND", "F_CHOP", "F_SAME", "F_APPEND",
			"F_SAME1").collect(Collectors.toCollection(LinkedHashSet::new));
	/**
	 * Opcodes of LABEL type. Also see {@link #OPS_FRAME}[0].
	 */
	public static final Set<String> OPS_LABEL = Stream.of("F_NEW").collect(Collectors.toCollection(LinkedHashSet::new));
	/**
	 * Opcodes of LABEL type. Also see {@link #OPS_FRAME}[0].
	 */
	public static final Set<String> OPS_LINE = Stream.of("F_NEW").collect(Collectors.toCollection(LinkedHashSet::new));
	/**
	 * Types of InvokeDynamic handle tags.
	 */
	public static final Set<String> OPS_TAG = Stream.of("H_GETFIELD", "H_GETSTATIC", "H_PUTFIELD", "H_PUTSTATIC",
			"H_INVOKEINTERFACE", "H_INVOKESPECIAL", "H_INVOKESTATIC", "H_INVOKEVIRTUAL", "H_NEWINVOKESPECIAL").collect(Collectors
					.toCollection(LinkedHashSet::new));
	private static final Set<Set<String>> INSN_SUBS = Stream.of(OPS_INSN_SUB_ARRAY, OPS_INSN_SUB_COMPARE, OPS_INSN_SUB_CONSTS,
			OPS_INSN_SUB_CONVERT, OPS_INSN_SUB_EXCEPTION, OPS_INSN_SUB_MATH, OPS_INSN_SUB_MONITOR, OPS_INSN_SUB_RETURN,
			OPS_INSN_SUB_STACK).collect(Collectors.toCollection(LinkedHashSet::new));
	/**
	 * Opcode type for custom opcodes.
	 */
	public static final int CUSTOM = 60;

	/**
	 * Converts an opcode name to its value.
	 * 
	 * @param name
	 *            Opcode name.
	 * @return Opcode value.
	 */
	public static int nameToOpcode(String name) {
		return nameToOpcode.get(name);
	}

	/**
	 * Converts an opcode value to its name.
	 * 
	 * @param op
	 *            Opcode value.
	 * @return Opcode name.
	 */
	public static String opcodeToName(int op) {
		return opcodeToName.get(op);
	}

	/**
	 * Converts an opcode <i>(Pertaining to frames)</i> name to its value.
	 * 
	 * @param name
	 *            Opcode name.
	 * @return Opcode value.
	 */
	public static int nameToFrame(String name) {
		return nameToFrame.get(name);
	}

	/**
	 * Converts an opcode <i>(Pertaining to frames)</i> value to its name.
	 * 
	 * @param op
	 *            Opcode value.
	 * @return Opcode name.
	 */
	public static String frameToName(int op) {
		return frameToName.get(op);
	}

	/**
	 * Converts a handle tag name to its value.
	 * 
	 * @param tag
	 *            Handle tag name.
	 * @return Handle tag value.
	 */
	public static int nameToTag(String tag) {
		return nameToTag.get(tag);
	}

	/**
	 * Converts a handle tag value to its name.
	 * 
	 * @param tag
	 *            Handle tag value.
	 * @return Handle tag name.
	 */
	public static String tagToName(int tag) {
		return tagToName.get(tag);
	}

	/**
	 * Retrieves the ASM type of the given opcode.
	 * 
	 * @param opcode
	 *            Opcode value.
	 * @return Type of opcode.
	 */
	public static int opcodeToType(int opcode) {
		return opcodeToType.get(opcode);
	}

	/**
	 * Retrieves the set of opcode names by the given opcode type.
	 * 
	 * @param type
	 *            Type of opcode
	 * @return Set of opcode names.
	 * 
	 * @see {@link org.objectweb.asm.tree.AbstractInsnNode AbstractInsnNode}:
	 *      <ul>
	 *      <li>{@link org.objectweb.asm.tree.AbstractInsnNode#FIELD_INSN
	 *      FIELD_INSN}</li>
	 *      <li>{@link org.objectweb.asm.tree.AbstractInsnNode#IINC_INSN
	 *      IINC_INSN}</li>
	 *      <li>{@link org.objectweb.asm.tree.AbstractInsnNode#INSN INSN}</li>
	 *      <li>{@link org.objectweb.asm.tree.AbstractInsnNode#INT_INSN
	 *      INT_INSN}</li>
	 *      <li>{@link org.objectweb.asm.tree.AbstractInsnNode#INVOKE_DYNAMIC_INSN
	 *      INVOKE_DYNAMIC_INSN}</li>
	 *      <li>{@link org.objectweb.asm.tree.AbstractInsnNode#JUMP_INSN
	 *      JUMP_INSN}</li>
	 *      <li>{@link org.objectweb.asm.tree.AbstractInsnNode#LDC_INSN
	 *      LDC_INSN}</li>
	 *      <li>{@link org.objectweb.asm.tree.AbstractInsnNode#LOOKUPSWITCH_INSN
	 *      LOOKUPSWITCH_INSN}</li>
	 *      <li>{@link org.objectweb.asm.tree.AbstractInsnNode#METHOD_INSN
	 *      METHOD_INSN}</li>
	 *      <li>{@link org.objectweb.asm.tree.AbstractInsnNode#MULTIANEWARRAY_INSN
	 *      MULTIANEWARRAY_INSN}</li>
	 *      <li>{@link org.objectweb.asm.tree.AbstractInsnNode#TABLESWITCH_INSN
	 *      TABLESWITCH_INSN}</li>
	 *      <li>{@link org.objectweb.asm.tree.AbstractInsnNode#TYPE_INSN
	 *      TYPE_INSN}</li>
	 *      <li>{@link org.objectweb.asm.tree.AbstractInsnNode#VAR_INSN
	 *      VAR_INSN}</li>
	 *      </ul>
	 */
	public static Set<String> typeToCodes(int type) {
		return insnTypeToCodes.get(type);
	}

	/**
	 * Return smaller subset of the {@link #OPS_INSN} set containing the given
	 * opcodes and others related to it.
	 * 
	 *
	 * @param insnOpName
	 *            The name of opcode
	 * @return The desired subset.
	 */
	public static Set<String> getInsnSubset(String insnOpName) {
		for (Set<String> set : INSN_SUBS) {
			if (set.contains(insnOpName)) {
				return set;
			}
		}
		// If not found, return empty set
		return Collections.emptySet();
	}

	private static void putOpcode(int op, String text) {
		nameToOpcode.put(text, op);
		opcodeToName.put(op, text);
	}

	private static void putFrame(int op, String text) {
		nameToFrame.put(text, op);
		frameToName.put(op, text);
	}

	private static void putTag(int op, String text) {
		nameToTag.put(text, op);
		tagToName.put(op, text);
	}

	private static void putType(int opcode, int type) {
		opcodeToType.put(opcode, type);
	}

	/**
	 * @param opcode
	 *            INSN type opcode.
	 * @return value of INSN type opcode. -2 if value could not be specified.
	 */
	public static int getValue(int opcode) {
		switch (opcode) {
		case ICONST_M1:
			return -1;
		case FCONST_0:
		case LCONST_0:
		case DCONST_0:
		case ICONST_0:
			return 0;
		case FCONST_1:
		case LCONST_1:
		case DCONST_1:
		case ICONST_1:
			return 1;
		case FCONST_2:
		case ICONST_2:
			return 2;
		case ICONST_3:
			return 3;
		case ICONST_4:
			return 4;
		case ICONST_5:
			return 5;
		}
		// No INSN type opcode has a value of -2, so this works.
		return -2;
	}

	static {
		insnTypeToCodes.put(AbstractInsnNode.FIELD_INSN, OpcodeUtil.OPS_FIELD);
		insnTypeToCodes.put(AbstractInsnNode.FRAME, OpcodeUtil.OPS_FRAME);
		insnTypeToCodes.put(AbstractInsnNode.IINC_INSN, OpcodeUtil.OPS_IINC);
		insnTypeToCodes.put(AbstractInsnNode.INSN, OpcodeUtil.OPS_INSN);
		insnTypeToCodes.put(AbstractInsnNode.INT_INSN, OpcodeUtil.OPS_INT);
		insnTypeToCodes.put(AbstractInsnNode.INVOKE_DYNAMIC_INSN, OpcodeUtil.OPS_INDY_METHOD);
		insnTypeToCodes.put(AbstractInsnNode.JUMP_INSN, OpcodeUtil.OPS_JUMP);
		insnTypeToCodes.put(AbstractInsnNode.LABEL, OpcodeUtil.OPS_LABEL);
		insnTypeToCodes.put(AbstractInsnNode.LDC_INSN, OpcodeUtil.OPS_LDC);
		insnTypeToCodes.put(AbstractInsnNode.LINE, OpcodeUtil.OPS_LINE);
		insnTypeToCodes.put(AbstractInsnNode.LOOKUPSWITCH_INSN, OpcodeUtil.OPS_LOOKUPSWITCH);
		insnTypeToCodes.put(AbstractInsnNode.METHOD_INSN, OpcodeUtil.OPS_METHOD);
		insnTypeToCodes.put(AbstractInsnNode.MULTIANEWARRAY_INSN, OpcodeUtil.OPS_MULTIANEWARRAY);
		insnTypeToCodes.put(AbstractInsnNode.TABLESWITCH_INSN, OpcodeUtil.OPS_TABLESWITCH);
		insnTypeToCodes.put(AbstractInsnNode.TYPE_INSN, OpcodeUtil.OPS_TYPE);
		insnTypeToCodes.put(AbstractInsnNode.VAR_INSN, OpcodeUtil.OPS_VAR);
		// Custom opcodes
		putType(NamedLabelNode.NAMED_LABEL, AbstractInsnNode.LABEL);
		putType(LineNumberNodeExt.LINE_EXT, AbstractInsnNode.LINE);
		putOpcode(NamedLabelNode.NAMED_LABEL, "LABEL");
		putOpcode(LineNumberNodeExt.LINE_EXT, "LINE");
		putOpcode(ParameterValInsnNode.PARAM_VAL, "PARAM");
		//
		putOpcode(AALOAD, "AALOAD");
		putOpcode(AASTORE, "AASTORE");
		putOpcode(ACONST_NULL, "ACONST_NULL");
		putOpcode(ALOAD, "ALOAD");
		putOpcode(ANEWARRAY, "ANEWARRAY");
		putOpcode(ARETURN, "ARETURN");
		putOpcode(ARRAYLENGTH, "ARRAYLENGTH");
		putOpcode(ASTORE, "ASTORE");
		putOpcode(ATHROW, "ATHROW");
		putOpcode(BALOAD, "BALOAD");
		putOpcode(BASTORE, "BASTORE");
		putOpcode(BIPUSH, "BIPUSH");
		putOpcode(CALOAD, "CALOAD");
		putOpcode(CASTORE, "CASTORE");
		putOpcode(CHECKCAST, "CHECKCAST");
		putOpcode(D2F, "D2F");
		putOpcode(D2I, "D2I");
		putOpcode(D2L, "D2L");
		putOpcode(DADD, "DADD");
		putOpcode(DALOAD, "DALOAD");
		putOpcode(DASTORE, "DASTORE");
		putOpcode(DCMPG, "DCMPG");
		putOpcode(DCMPL, "DCMPL");
		putOpcode(DCONST_0, "DCONST_0");
		putOpcode(DCONST_1, "DCONST_1");
		putOpcode(DDIV, "DDIV");
		putOpcode(DLOAD, "DLOAD");
		putOpcode(DMUL, "DMUL");
		putOpcode(DNEG, "DNEG");
		putOpcode(DREM, "DREM");
		putOpcode(DRETURN, "DRETURN");
		putOpcode(DSTORE, "DSTORE");
		putOpcode(DSUB, "DSUB");
		putOpcode(DUP, "DUP");
		putOpcode(DUP2, "DUP2");
		putOpcode(DUP2_X1, "DUP2_X1");
		putOpcode(DUP2_X2, "DUP2_X2");
		putOpcode(DUP_X1, "DUP_X1");
		putOpcode(DUP_X2, "DUP_X2");
		putOpcode(F2D, "F2D");
		putOpcode(F2I, "F2I");
		putOpcode(F2L, "F2L");
		putOpcode(F_NEW, "F_NEW");
		putOpcode(FADD, "FADD");
		putOpcode(FALOAD, "FALOAD");
		putOpcode(FASTORE, "FASTORE");
		putOpcode(FCMPG, "FCMPG");
		putOpcode(FCMPL, "FCMPL");
		putOpcode(FCONST_0, "FCONST_0");
		putOpcode(FCONST_1, "FCONST_1");
		putOpcode(FCONST_2, "FCONST_2");
		putOpcode(FDIV, "FDIV");
		putOpcode(FLOAD, "FLOAD");
		putOpcode(FMUL, "FMUL");
		putOpcode(FNEG, "FNEG");
		putOpcode(FREM, "FREM");
		putOpcode(FRETURN, "FRETURN");
		putOpcode(FSTORE, "FSTORE");
		putOpcode(FSUB, "FSUB");
		putOpcode(GETFIELD, "GETFIELD");
		putOpcode(GETSTATIC, "GETSTATIC");
		putOpcode(GOTO, "GOTO");
		putOpcode(I2B, "I2B");
		putOpcode(I2C, "I2C");
		putOpcode(I2D, "I2D");
		putOpcode(I2F, "I2F");
		putOpcode(I2L, "I2L");
		putOpcode(I2S, "I2S");
		putOpcode(IADD, "IADD");
		putOpcode(IALOAD, "IALOAD");
		putOpcode(IAND, "IAND");
		putOpcode(IASTORE, "IASTORE");
		putOpcode(ICONST_0, "ICONST_0");
		putOpcode(ICONST_1, "ICONST_1");
		putOpcode(ICONST_2, "ICONST_2");
		putOpcode(ICONST_3, "ICONST_3");
		putOpcode(ICONST_4, "ICONST_4");
		putOpcode(ICONST_5, "ICONST_5");
		putOpcode(ICONST_M1, "ICONST_M1");
		putOpcode(IDIV, "IDIV");
		putOpcode(IF_ACMPEQ, "IF_ACMPEQ");
		putOpcode(IF_ACMPNE, "IF_ACMPNE");
		putOpcode(IF_ICMPEQ, "IF_ICMPEQ");
		putOpcode(IF_ICMPGE, "IF_ICMPGE");
		putOpcode(IF_ICMPGT, "IF_ICMPGT");
		putOpcode(IF_ICMPLE, "IF_ICMPLE");
		putOpcode(IF_ICMPLT, "IF_ICMPLT");
		putOpcode(IF_ICMPNE, "IF_ICMPNE");
		putOpcode(IFEQ, "IFEQ");
		putOpcode(IFGE, "IFGE");
		putOpcode(IFGT, "IFGT");
		putOpcode(IFLE, "IFLE");
		putOpcode(IFLT, "IFLT");
		putOpcode(IFNE, "IFNE");
		putOpcode(IFNONNULL, "IFNONNULL");
		putOpcode(IFNULL, "IFNULL");
		putOpcode(IINC, "IINC");
		putOpcode(ILOAD, "ILOAD");
		putOpcode(IMUL, "IMUL");
		putOpcode(INEG, "INEG");
		putOpcode(INSTANCEOF, "INSTANCEOF");
		putOpcode(INVOKEDYNAMIC, "INVOKEDYNAMIC");
		putOpcode(INVOKEINTERFACE, "INVOKEINTERFACE");
		putOpcode(INVOKESPECIAL, "INVOKESPECIAL");
		putOpcode(INVOKESTATIC, "INVOKESTATIC");
		putOpcode(INVOKEVIRTUAL, "INVOKEVIRTUAL");
		putOpcode(IOR, "IOR");
		putOpcode(IREM, "IREM");
		putOpcode(IRETURN, "IRETURN");
		putOpcode(ISHL, "ISHL");
		putOpcode(ISHR, "ISHR");
		putOpcode(ISTORE, "ISTORE");
		putOpcode(ISUB, "ISUB");
		putOpcode(IUSHR, "IUSHR");
		putOpcode(IXOR, "IXOR");
		putOpcode(JSR, "JSR");
		putOpcode(L2D, "L2D");
		putOpcode(L2F, "L2F");
		putOpcode(L2I, "L2I");
		putOpcode(LADD, "LADD");
		putOpcode(LALOAD, "LALOAD");
		putOpcode(LAND, "LAND");
		putOpcode(LASTORE, "LASTORE");
		putOpcode(LCMP, "LCMP");
		putOpcode(LCONST_0, "LCONST_0");
		putOpcode(LCONST_1, "LCONST_1");
		putOpcode(LDC, "LDC");
		putOpcode(LDIV, "LDIV");
		putOpcode(LLOAD, "LLOAD");
		putOpcode(LMUL, "LMUL");
		putOpcode(LNEG, "LNEG");
		putOpcode(LOOKUPSWITCH, "LOOKUPSWITCH");
		putOpcode(LOR, "LOR");
		putOpcode(LREM, "LREM");
		putOpcode(LRETURN, "LRETURN");
		putOpcode(LSHL, "LSHL");
		putOpcode(LSHR, "LSHR");
		putOpcode(LSTORE, "LSTORE");
		putOpcode(LSUB, "LSUB");
		putOpcode(LUSHR, "LUSHR");
		putOpcode(LXOR, "LXOR");
		putOpcode(MONITORENTER, "MONITORENTER");
		putOpcode(MONITOREXIT, "MONITOREXIT");
		putOpcode(MULTIANEWARRAY, "MULTIANEWARRAY");
		putOpcode(NEW, "NEW");
		putOpcode(NEWARRAY, "NEWARRAY");
		putOpcode(NOP, "NOP");
		putOpcode(POP, "POP");
		putOpcode(POP2, "POP2");
		putOpcode(PUTFIELD, "PUTFIELD");
		putOpcode(PUTSTATIC, "PUTSTATIC");
		putOpcode(RET, "RET");
		putOpcode(RETURN, "RETURN");
		putOpcode(SALOAD, "SALOAD");
		putOpcode(SASTORE, "SASTORE");
		putOpcode(SIPUSH, "SIPUSH");
		putOpcode(SWAP, "SWAP");
		putOpcode(TABLESWITCH, "TABLESWITCH");
		putFrame(F_APPEND, "F_APPEND");
		putFrame(F_APPEND, "F_APPEND");
		putFrame(F_CHOP, "F_CHOP");
		putFrame(F_FULL, "F_FULL");
		putFrame(F_NEW, "F_NEW");
		putFrame(F_SAME, "F_SAME");
		putFrame(F_SAME1, "F_SAME1");
		putTag(Opcodes.H_GETFIELD, "H_GETFIELD");
		putTag(Opcodes.H_GETSTATIC, "H_GETSTATIC");
		putTag(Opcodes.H_INVOKEINTERFACE, "H_INVOKEINTERFACE");
		putTag(Opcodes.H_INVOKESPECIAL, "H_INVOKESPECIAL");
		putTag(Opcodes.H_INVOKESTATIC, "H_INVOKESTATIC");
		putTag(Opcodes.H_INVOKEVIRTUAL, "H_INVOKEVIRTUAL");
		putTag(Opcodes.H_NEWINVOKESPECIAL, "H_NEWINVOKESPECIAL");
		putTag(Opcodes.H_PUTFIELD, "H_PUTFIELD");
		putTag(Opcodes.H_PUTSTATIC, "H_PUTSTATIC");
		putType(NOP, AbstractInsnNode.INSN);
		putType(ACONST_NULL, AbstractInsnNode.INSN);
		putType(ICONST_M1, AbstractInsnNode.INSN);
		putType(ICONST_0, AbstractInsnNode.INSN);
		putType(ICONST_1, AbstractInsnNode.INSN);
		putType(ICONST_2, AbstractInsnNode.INSN);
		putType(ICONST_3, AbstractInsnNode.INSN);
		putType(ICONST_4, AbstractInsnNode.INSN);
		putType(ICONST_5, AbstractInsnNode.INSN);
		putType(LCONST_0, AbstractInsnNode.INSN);
		putType(LCONST_1, AbstractInsnNode.INSN);
		putType(FCONST_0, AbstractInsnNode.INSN);
		putType(FCONST_1, AbstractInsnNode.INSN);
		putType(FCONST_2, AbstractInsnNode.INSN);
		putType(DCONST_0, AbstractInsnNode.INSN);
		putType(DCONST_1, AbstractInsnNode.INSN);
		putType(IALOAD, AbstractInsnNode.INSN);
		putType(LALOAD, AbstractInsnNode.INSN);
		putType(FALOAD, AbstractInsnNode.INSN);
		putType(DALOAD, AbstractInsnNode.INSN);
		putType(AALOAD, AbstractInsnNode.INSN);
		putType(BALOAD, AbstractInsnNode.INSN);
		putType(CALOAD, AbstractInsnNode.INSN);
		putType(SALOAD, AbstractInsnNode.INSN);
		putType(IASTORE, AbstractInsnNode.INSN);
		putType(LASTORE, AbstractInsnNode.INSN);
		putType(FASTORE, AbstractInsnNode.INSN);
		putType(DASTORE, AbstractInsnNode.INSN);
		putType(AASTORE, AbstractInsnNode.INSN);
		putType(BASTORE, AbstractInsnNode.INSN);
		putType(CASTORE, AbstractInsnNode.INSN);
		putType(SASTORE, AbstractInsnNode.INSN);
		putType(POP, AbstractInsnNode.INSN);
		putType(POP2, AbstractInsnNode.INSN);
		putType(DUP, AbstractInsnNode.INSN);
		putType(DUP_X1, AbstractInsnNode.INSN);
		putType(DUP_X2, AbstractInsnNode.INSN);
		putType(DUP2, AbstractInsnNode.INSN);
		putType(DUP2_X1, AbstractInsnNode.INSN);
		putType(DUP2_X2, AbstractInsnNode.INSN);
		putType(SWAP, AbstractInsnNode.INSN);
		putType(IADD, AbstractInsnNode.INSN);
		putType(LADD, AbstractInsnNode.INSN);
		putType(FADD, AbstractInsnNode.INSN);
		putType(DADD, AbstractInsnNode.INSN);
		putType(ISUB, AbstractInsnNode.INSN);
		putType(LSUB, AbstractInsnNode.INSN);
		putType(FSUB, AbstractInsnNode.INSN);
		putType(DSUB, AbstractInsnNode.INSN);
		putType(IMUL, AbstractInsnNode.INSN);
		putType(LMUL, AbstractInsnNode.INSN);
		putType(FMUL, AbstractInsnNode.INSN);
		putType(DMUL, AbstractInsnNode.INSN);
		putType(IDIV, AbstractInsnNode.INSN);
		putType(LDIV, AbstractInsnNode.INSN);
		putType(FDIV, AbstractInsnNode.INSN);
		putType(DDIV, AbstractInsnNode.INSN);
		putType(IREM, AbstractInsnNode.INSN);
		putType(LREM, AbstractInsnNode.INSN);
		putType(FREM, AbstractInsnNode.INSN);
		putType(DREM, AbstractInsnNode.INSN);
		putType(INEG, AbstractInsnNode.INSN);
		putType(LNEG, AbstractInsnNode.INSN);
		putType(FNEG, AbstractInsnNode.INSN);
		putType(DNEG, AbstractInsnNode.INSN);
		putType(ISHL, AbstractInsnNode.INSN);
		putType(LSHL, AbstractInsnNode.INSN);
		putType(ISHR, AbstractInsnNode.INSN);
		putType(LSHR, AbstractInsnNode.INSN);
		putType(IUSHR, AbstractInsnNode.INSN);
		putType(LUSHR, AbstractInsnNode.INSN);
		putType(IAND, AbstractInsnNode.INSN);
		putType(LAND, AbstractInsnNode.INSN);
		putType(IOR, AbstractInsnNode.INSN);
		putType(LOR, AbstractInsnNode.INSN);
		putType(IXOR, AbstractInsnNode.INSN);
		putType(LXOR, AbstractInsnNode.INSN);
		putType(I2L, AbstractInsnNode.INSN);
		putType(I2F, AbstractInsnNode.INSN);
		putType(I2D, AbstractInsnNode.INSN);
		putType(L2I, AbstractInsnNode.INSN);
		putType(L2F, AbstractInsnNode.INSN);
		putType(L2D, AbstractInsnNode.INSN);
		putType(F2I, AbstractInsnNode.INSN);
		putType(F2L, AbstractInsnNode.INSN);
		putType(F2D, AbstractInsnNode.INSN);
		putType(D2I, AbstractInsnNode.INSN);
		putType(D2L, AbstractInsnNode.INSN);
		putType(D2F, AbstractInsnNode.INSN);
		putType(I2B, AbstractInsnNode.INSN);
		putType(I2C, AbstractInsnNode.INSN);
		putType(I2S, AbstractInsnNode.INSN);
		putType(LCMP, AbstractInsnNode.INSN);
		putType(FCMPL, AbstractInsnNode.INSN);
		putType(FCMPG, AbstractInsnNode.INSN);
		putType(DCMPL, AbstractInsnNode.INSN);
		putType(DCMPG, AbstractInsnNode.INSN);
		putType(IRETURN, AbstractInsnNode.INSN);
		putType(LRETURN, AbstractInsnNode.INSN);
		putType(FRETURN, AbstractInsnNode.INSN);
		putType(DRETURN, AbstractInsnNode.INSN);
		putType(ARETURN, AbstractInsnNode.INSN);
		putType(RETURN, AbstractInsnNode.INSN);
		putType(ARRAYLENGTH, AbstractInsnNode.INSN);
		putType(ATHROW, AbstractInsnNode.INSN);
		putType(MONITORENTER, AbstractInsnNode.INSN);
		putType(MONITOREXIT, AbstractInsnNode.INSN);
		putType(BIPUSH, AbstractInsnNode.INT_INSN);
		putType(SIPUSH, AbstractInsnNode.INT_INSN);
		putType(NEWARRAY, AbstractInsnNode.INT_INSN);
		putType(ILOAD, AbstractInsnNode.VAR_INSN);
		putType(LLOAD, AbstractInsnNode.VAR_INSN);
		putType(FLOAD, AbstractInsnNode.VAR_INSN);
		putType(DLOAD, AbstractInsnNode.VAR_INSN);
		putType(ALOAD, AbstractInsnNode.VAR_INSN);
		putType(ISTORE, AbstractInsnNode.VAR_INSN);
		putType(LSTORE, AbstractInsnNode.VAR_INSN);
		putType(FSTORE, AbstractInsnNode.VAR_INSN);
		putType(DSTORE, AbstractInsnNode.VAR_INSN);
		putType(ASTORE, AbstractInsnNode.VAR_INSN);
		putType(RET, AbstractInsnNode.VAR_INSN);
		putType(NEW, AbstractInsnNode.TYPE_INSN);
		putType(ANEWARRAY, AbstractInsnNode.TYPE_INSN);
		putType(CHECKCAST, AbstractInsnNode.TYPE_INSN);
		putType(INSTANCEOF, AbstractInsnNode.TYPE_INSN);
		putType(GETSTATIC, AbstractInsnNode.FIELD_INSN);
		putType(GETFIELD, AbstractInsnNode.FIELD_INSN);
		putType(PUTSTATIC, AbstractInsnNode.FIELD_INSN);
		putType(PUTFIELD, AbstractInsnNode.FIELD_INSN);
		putType(INVOKEVIRTUAL, AbstractInsnNode.METHOD_INSN);
		putType(INVOKESPECIAL, AbstractInsnNode.METHOD_INSN);
		putType(INVOKESTATIC, AbstractInsnNode.METHOD_INSN);
		putType(INVOKEINTERFACE, AbstractInsnNode.METHOD_INSN);
		putType(INVOKEDYNAMIC, AbstractInsnNode.INVOKE_DYNAMIC_INSN);
		putType(IFEQ, AbstractInsnNode.JUMP_INSN);
		putType(IFNE, AbstractInsnNode.JUMP_INSN);
		putType(IFLT, AbstractInsnNode.JUMP_INSN);
		putType(IFGE, AbstractInsnNode.JUMP_INSN);
		putType(IFGT, AbstractInsnNode.JUMP_INSN);
		putType(IFLE, AbstractInsnNode.JUMP_INSN);
		putType(IF_ICMPEQ, AbstractInsnNode.JUMP_INSN);
		putType(IF_ICMPNE, AbstractInsnNode.JUMP_INSN);
		putType(IF_ICMPLT, AbstractInsnNode.JUMP_INSN);
		putType(IF_ICMPGE, AbstractInsnNode.JUMP_INSN);
		putType(IF_ICMPGT, AbstractInsnNode.JUMP_INSN);
		putType(IF_ICMPLE, AbstractInsnNode.JUMP_INSN);
		putType(IF_ACMPEQ, AbstractInsnNode.JUMP_INSN);
		putType(IF_ACMPNE, AbstractInsnNode.JUMP_INSN);
		putType(GOTO, AbstractInsnNode.JUMP_INSN);
		putType(JSR, AbstractInsnNode.JUMP_INSN);
		putType(IFNULL, AbstractInsnNode.JUMP_INSN);
		putType(IFNONNULL, AbstractInsnNode.JUMP_INSN);
		putType(LDC, AbstractInsnNode.LDC_INSN);
		putType(IINC, AbstractInsnNode.IINC_INSN);
		putType(TABLESWITCH, AbstractInsnNode.TABLESWITCH_INSN);
		putType(LOOKUPSWITCH, AbstractInsnNode.LOOKUPSWITCH_INSN);
		putType(MULTIANEWARRAY, AbstractInsnNode.MULTIANEWARRAY_INSN);
	}

	/**
	 * Calculate the index of an opcode.
	 * 
	 * @param ain
	 *            Opcode.
	 * @return Opcode index.
	 */
	public static int index(AbstractInsnNode ain) {
		return index(ain, null);
	}

	/**
	 * Calculate the index of an opcode in the given method.
	 * 
	 * @param ain
	 *            Opcode.
	 * @param method
	 *            Method containing the opcode.
	 * @return Opcode index.
	 */
	public static int index(AbstractInsnNode ain, MethodNode method) {
		// method should cache the index
		if (method != null) {
			return method.instructions.indexOf(ain);
		}
		// calculate manually
		int index = 0;
		while (ain.getPrevious() != null) {
			ain = ain.getPrevious();
			index++;
		}
		return index;
	}

	/**
	 * Size of a method <i>(calculated via opcode linked list)</i>
	 * 
	 * @param ain
	 *            The opcode.
	 * @return Size of method.
	 */
	public static int getSize(AbstractInsnNode ain) {
		return getSize(ain, null);
	}

	/**
	 * Size of a method.
	 * 
	 * @param method
	 *            The method.
	 * @return Size.
	 */
	public static int getSize(MethodNode method) {
		return getSize(null, method);
	}

	/**
	 * Size of a method.
	 * 
	 * @param ain
	 *            The opcode.
	 * @param method
	 *            The method.
	 * @return -1 if could not be calculated. Would only occur when
	 *         {@code (ain == null && method == null)}
	 */
	public static int getSize(AbstractInsnNode ain, MethodNode method) {
		if (method != null) {
			return method.instructions.size();
		}
		if (ain == null) {
			return -1;
		}
		int size = index(ain);
		while (ain.getNext() != null) {
			ain = ain.getNext();
			size++;
		}
		return size;
	}

	/**
	 * Number of matching types at and before the given opcode.
	 * 
	 * @param type
	 *            Type to check for.
	 * @param ain
	 *            Start index.
	 * @return Number of matching types.
	 */
	public static int count(int type, AbstractInsnNode ain) {
		int count = type == ain.getType() ? 1 : 0;
		if (ain.getPrevious() != null) {
			count += count(type, ain.getPrevious());
		}
		return count;
	}

	/**
	 * @param ain
	 *            Label opcode.
	 * @return Generated label name.
	 */
	public static String labelName(AbstractInsnNode ain) {
		if (ain instanceof NamedLabelNode) {
			return ((NamedLabelNode) ain).name;
		}
		return NamedLabelNode.generateName(count(AbstractInsnNode.LABEL, ain) - 1);
	}

	/**
	 * Links two given insns together via their linked list's previous and next
	 * values. Removing individual items in large changes is VERY slow, so while
	 * this may be ugly its worth it.
	 * 
	 * @param instructions
	 * @param insnStart
	 * @param insnEnd
	 */
	public static void link(InsnList instructions, AbstractInsnNode insnStart, AbstractInsnNode insnEnd) {
		try {
			boolean first = instructions.getFirst().equals(insnStart);
			Field next = Reflect.getField(AbstractInsnNode.class, "nextInsn", "next");
			Field prev = Reflect.getField(AbstractInsnNode.class, "previousInsn", "prev");
			next.setAccessible(true);
			prev.setAccessible(true);
			if (first) {
				// Update head
				Field listStart = Reflect.getField(InsnList.class, "firstInsn", "first");
				listStart.setAccessible(true);
				listStart.set(instructions, insnEnd.getNext());
				// Remove link to previous sections
				prev.set(insnEnd.getNext(), null);
			} else {
				// insnStart.prev links to insnEnd.next
				next.set(insnStart.getPrevious(), insnEnd.getNext());
				prev.set(insnEnd.getNext(), insnStart.getPrevious());
			}
			// Reset cache
			Field listStart = InsnList.class.getDeclaredField("cache");
			listStart.setAccessible(true);
			listStart.set(instructions, null);
		} catch (Exception e) {
			Logging.error(e, false);
		}
	}

	/**
	 * @param ain
	 *            Opcode to check.
	 * @return Opcode is not linked to any other node.
	 */
	public static boolean isolated(AbstractInsnNode ain) {
		return ain.getNext() == null && ain.getPrevious() == null;
	}
}
