package randoop.fieldextensions;

import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;


import extensions.FieldExtensionsCollector;
import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.main.GenInputsAbstract;
import randoop.operation.TypedOperation;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Statement;
import utils.Tuple;


public class CanonicalStringsRedundancy extends GlobalExtensionsRedundancy {
	
	private Map<String, Set<String>> canStrs = new LinkedHashMap<>();
	
	FileWriter objWriter;
	FileWriter seqWriter;
	
	// classesUnderTest = null to consider all classes as relevant
	public CanonicalStringsRedundancy(int maxStoppingObjs, int maxStoppingPrims, int maxStoppingArr,
			Pattern omitfields, String outputObjs, String outputObjSeqs) {
		super(maxStoppingObjs, maxStoppingPrims, maxStoppingArr, omitfields);
		
		try {
			if (outputObjs != null)
				objWriter = new FileWriter(outputObjs);
			if (outputObjSeqs != null)
				seqWriter = new FileWriter(outputObjSeqs);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	// Returns the indices where the objects that initialize new field values are, null if 
	// some object exceeds given bounds or the execution of the sequence fails 
	@Override
	protected Set<Integer> newFieldValuesInitialized(ExecutableSequence sequence) {
		Set<Integer> indices = new LinkedHashSet<>();
		int i = sequence.sequence.size() -1;

		ExecutionOutcome statementResult = sequence.getResult(i);	
		TypedOperation op = sequence.sequence.getStatement(i).getOperation();

		//String className = Utils.getOperationClass(op);
		//if (!Utils.classUnderTest(className)) return;
		
		if (statementResult instanceof NormalExecution) {
			int index = 0;
			Statement stmt = sequence.sequence.getStatement(i);
			// Sort objects by type first
			Map<String, List<Tuple<Object, Integer>>> objsByType = new LinkedHashMap<>();
			if (!stmt.getOutputType().isVoid()) {
				Object retVal = ((NormalExecution)statementResult).getRuntimeValue();
				if (retVal != null && !Utils.isPrimitive(retVal)) {
					List<Tuple<Object, Integer>> l = new LinkedList<>();
					l.add(new Tuple<>(retVal, index));
					objsByType.put(retVal.getClass().getName(), l);
				}
				index++;
			}

			Object[] objsAfterExec = sequence.getRuntimeInputs(i);
			for (int j = 0; j < objsAfterExec.length; j++) {
				Object curr = objsAfterExec[j];
				if (curr == null || Utils.isPrimitive(curr)) { index++; continue; }
				String cls = curr.getClass().getName();
				if (objsByType.get(cls) == null) {
					List<Tuple<Object, Integer>> l = new LinkedList<>();
					l.add(new Tuple<>(curr, index));;
					objsByType.put(cls, l);
				}
				else 
					objsByType.get(cls).add(new Tuple<>(curr, index));
				index++;
			}	
			
			// Test does not exceed the limits
			for (String cls: objsByType.keySet()) {
				// FIXME: We currently do not keep track of the exact object that has extended the extensions
				// but return all indices of the objects for the class for which we saw new field values
				for (Tuple<Object, Integer> t: objsByType.get(cls)) {
					FieldExtensionsCollector col = new FieldExtensionsCollector();
					if (!canonicalizer.canonicalize(t.getFirst(), col))
						return null;
					String objCanStr = col.getExtensions().toSortedString();
					if (addString(cls, objCanStr)) {
						indices.add(t.getSecond());
						
						if (objWriter != null)  {
							try {
								String objStr = t.getFirst().toString();
								objWriter.write(objStr + "\n");

								if (seqWriter != null) {
									seqWriter.write("---\n");
									seqWriter.write(objStr + "\n");
									seqWriter.write("index: " + t.getSecond() + "\n");
									seqWriter.write(sequence.toCodeString() + "\n");
									if (GenInputsAbstract.output_full_extensions) {
										seqWriter.write(objCanStr + "\n");
									}
									seqWriter.write("---\n");
								}
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
			}	
		}
		else {
			// Abnormal execution
			throw new Error("Computing active indices for an invalid sequence");
		}

		return indices;
	}
	
	public boolean addString(String key, String objCanStr) {
		Set<String> keyCanStrs = canStrs.get(key);
		if (keyCanStrs == null) {
			keyCanStrs = new LinkedHashSet<>();
			canStrs.put(key, keyCanStrs);
		}
		
		return keyCanStrs.add(objCanStr);
	}
	
	@Override
	public void writeResults(String filename, boolean fullres) {
		try {
			FileWriter writer = new FileWriter(filename);
			writer.write(outputExt.getStatistics(fullres));
			int totalObjs = 0;
			for (String cls: canStrs.keySet()) {
				writer.write(cls + " objects: " + canStrs.get(cls).size() + "\n");
				totalObjs += canStrs.get(cls).size();
			}
			writer.write("Total objects sum: "+ totalObjs + "\n");
			writer.close();
			
			if (objWriter != null)  
				objWriter.close();
			
			if (seqWriter != null)  
				seqWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}



}
