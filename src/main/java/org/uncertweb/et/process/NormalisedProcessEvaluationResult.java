package org.uncertweb.et.process;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;
import org.uncertweb.et.design.NormalisedDesign;

/**
 * ridiculous code duplication from {@link NormalisedDesign}
 * 
 * @author r_jones
 *
 */
public class NormalisedProcessEvaluationResult extends ProcessEvaluationResult {

	private Map<String, Double> meanMap;
	private Map<String, Double> stdDevMap;
	
	public NormalisedProcessEvaluationResult() {
		super();
	}
	
	public void addResults(String outputIdentifier, Double[] results, Double mean, Double stdDev) {
		if (!outputIdentifiers.contains(outputIdentifier)) {
			outputIdentifiers.add(outputIdentifier);
		}
		addResults(outputIdentifier, results);
		meanMap.put(outputIdentifier, mean);
		stdDevMap.put(outputIdentifier, stdDev);
	}

	private NormalisedProcessEvaluationResult(ProcessEvaluationResult original, double[] mean, double[] stdDev) {
		// construct
		super();
		this.meanMap = new HashMap<String, Double>();
		this.stdDevMap = new HashMap<String, Double>();

		// get identifiers
		List<String> identifiers = original.getOutputIdentifiers();

		// normalise
		for (int i = 0; i < identifiers.size(); i++) {
			String current = identifiers.get(i);
			Double currentMean = mean[i];
			this.meanMap.put(current, currentMean);
			Double currentStdDev = stdDev[i];
			this.stdDevMap.put(current, currentStdDev);
			Double[] results = original.getResults(current);
			Double[] normalised = new Double[results.length];
			for (int j = 0; j < normalised.length; j++) {
				normalised[j] = (results[j] - currentMean) / currentStdDev;
			}
			this.addResults(current, normalised);
		}
	}

	public static NormalisedProcessEvaluationResult fromProcessEvaluationResult(ProcessEvaluationResult result) {
		List<String> identifiers = result.getOutputIdentifiers();

		// compute mean and stddev
		Mean m = new Mean();
		StandardDeviation sd = new StandardDeviation();
		double[] mean = new double[result.getSize()];
		double[] stdDev = new double[result.getSize()];
		for (int i = 0; i < identifiers.size(); i++) {
			double[] primitive = ArrayUtils.toPrimitive(result.getResults(identifiers.get(i)));
			mean[i] = m.evaluate(primitive);
			stdDev[i] = sd.evaluate(primitive);
		}

		// return
		return new NormalisedProcessEvaluationResult(result, mean, stdDev);
	}

	public static NormalisedProcessEvaluationResult fromProcessEvaluationResult(ProcessEvaluationResult result, double[] mean, double[] stdDev) {
		return new NormalisedProcessEvaluationResult(result, mean, stdDev);
	}

	public double getMean(String inputIdentifier) {
		return meanMap.get(inputIdentifier);
	}

	public double getStdDev(String inputIdentifier) {
		return stdDevMap.get(inputIdentifier);
	}

	public ProcessEvaluationResult unnormalise() {
		// create design
		ProcessEvaluationResult result = new ProcessEvaluationResult();

		// get identifiers
		List<String> identifiers = this.getOutputIdentifiers();

		// unnormalise each
		for (int i = 0; i < identifiers.size(); i++) {
			String current = identifiers.get(i);
			Double currentMean = this.meanMap.get(current);
			Double currentStdDev = this.stdDevMap.get(current);
			Double[] normalised = this.getResults(current);
			Double[] results = new Double[normalised.length];
			for (int j = 0; j < results.length; j++) {
				results[j] = normalised[j] * currentStdDev + currentMean;
			}
			result.addResults(current, results);
		}

		return result;
	}

}