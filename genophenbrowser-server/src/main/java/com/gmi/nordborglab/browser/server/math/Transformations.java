package com.gmi.nordborglab.browser.server.math;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/25/13
 * Time: 11:38 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class Transformations {

    public abstract static class TransformFunc implements Function<Double,Double> {

        protected final Double minValue;

        protected final Double variance;
        public TransformFunc(final Double minValue,final Double variance) {
            this.minValue = minValue;
            this.variance = variance;
        }

    }
    public static class LogTransformFunc extends TransformFunc {

        public LogTransformFunc(Double minValue, Double stdVariance) {
            super(minValue, stdVariance);
        }


        @Nullable
        @Override
        public Double apply(@Nullable Double input) {
             if (input == null)
                 return null;
            return Math.log(input - minValue + variance * 0.1);
        }

    }
    public static class SqrtTransformFunc extends TransformFunc {

        public SqrtTransformFunc(Double minValue, Double stdVariance) {
            super(minValue, stdVariance);
        }
        @Nullable
        @Override
        public Double apply(@Nullable Double input) {
            if (input == null)
                return null;
            return Math.sqrt(input - minValue + variance * 0.1);
        }

    }
    public static class BoxCoxTransformFunc extends TransformFunc {

        public BoxCoxTransformFunc(Double minValue, Double stdVariance) {
            super(minValue, stdVariance);
        }
        @Nullable
        @Override
        public Double apply(@Nullable Double input) {
            if (input == null)
                return null;
            return input - minValue + variance * 0.1;
        }

    }
    public static List<Double> logTransform(List<Double> values) {
        DescriptiveStatistics stats = new DescriptiveStatistics(Doubles.toArray(values));
        Double variance =  stats.getVariance();
        Double min = stats.getMin();
        return Lists.transform(values, new LogTransformFunc(min, variance));
    }


    public static List<Double> sqrtTransform(List<Double> values) {
        DescriptiveStatistics stats = new DescriptiveStatistics(Doubles.toArray(values));
        Double variance =  stats.getVariance();
        Double min = stats.getMin();
        return Lists.transform(values, new SqrtTransformFunc(min, variance));
    }

    public static List<Double> boxCoxTransform(List<Double> values) {
        return values;
    }

    public static Double calculateShapiroPval(List<Double> values) {
      return 0.0;
    }
}
