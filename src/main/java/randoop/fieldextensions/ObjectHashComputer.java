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

import org.antlr.runtime.TokenSource;

import canonicalizer.BFHeapCanonicalizer;
import extensions.BoundedFieldExtensionsCollector;
import extensions.FieldExtensionsCollector;
import extensions.IFieldExtensions;
import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.generation.ComponentManager;
import randoop.main.GenInputsAbstract;
import randoop.operation.TypedOperation;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Statement;
import utils.Tuple;


public class ObjectHashComputer extends BoundedExtensionsComputer {
	
	private Map<String, Set<Integer>> hashes = new LinkedHashMap<>();
	
	FileWriter objWriter;
	FileWriter seqWriter;
	
	// classesUnderTest = null to consider all classes as relevant
	public ObjectHashComputer(int maxStoppingObjs, int maxStoppingPrims, 
			IBuildersManager buildersManager, Pattern omitfields, 
			String outputObjs, String outputObjSeqs) {
		super(maxStoppingObjs, maxStoppingPrims, buildersManager, omitfields);
		
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
			
			for (String cls: objsByType.keySet()) {
				FieldExtensionsCollector collector = outputExt.getOrCreateCollectorForMethodParam(cls);
				collector.start();
				collector.setTestMode();
				for (Tuple<Object, Integer> t: objsByType.get(cls)) {
					if (!canonicalizer.canonicalize(t.getFirst(), collector))
						return null;
				}
				collector.testCommitAllPairs();
				if (collector.testExtensionsLimitExceeded())
					return null;
			}
			
			// Test does not exceed the limits
			for (String cls: objsByType.keySet()) {
				FieldExtensionsCollector collector = outputExt.getOrCreateCollectorForMethodParam(cls);
				if (collector.testExtensionsWereExtended()) {
					collector.commitSuccessfulTestsPairs();
				}
				// FIXME: We currently do not keep track of the exact object that has extended the extensions
				// but return all indices of the objects for the class for which we saw new field values
				for (Tuple<Object, Integer> t: objsByType.get(cls)) {
					FieldExtensionsCollector col = new FieldExtensionsCollector();
					if (!canonicalizer.canonicalize(t.getFirst(), col))
						return null;
					//int hash = col.getExtensions().toString().hashCode();
					int hash = col.getExtensions().toString().hashCode();
					if (addHash(cls, hash)) {
						indices.add(t.getSecond());
						
						if (objWriter != null)  {
							try {
								String objStr = t.getFirst().toString();
								objWriter.write(objStr + "\n");

								if (seqWriter != null) {
									seqWriter.write("---\n");
									seqWriter.write(objStr + "\n");
									seqWriter.write("index: " + t.getSecond() + "\n");
									seqWriter.write(sequence.toString() + "\n");
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
			return null;
		}

		return indices;
	}
	
	public boolean addHash(String key, Integer hash) {
		Set<Integer> keyHashes = hashes.get(key);
		if (keyHashes == null) {
			keyHashes = new LinkedHashSet<>();
			hashes.put(key, keyHashes);
		}
		
		return keyHashes.add(hash);
	}
	
	@Override
	public void writeResults(String filename, boolean fullres) {
		try {
			FileWriter writer = new FileWriter(filename);
			writer.write(outputExt.getStatistics(fullres));
			int totalObjs = 0;
			for (String cls: hashes.keySet()) {
				writer.write(cls + " objects: " + hashes.get(cls).size() + "\n");
				totalObjs += hashes.get(cls).size();
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
