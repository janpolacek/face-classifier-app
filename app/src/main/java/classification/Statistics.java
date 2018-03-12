package classification;

public class Statistics {
    private float[] data;
    private int size;
    private float mean = 0;
    private float variance = 0;
    private float std = 0;

    public Statistics(float[] data) {
        this.data = data;
        size = data.length;
    }

    float getMean() {
        if(mean != 0){
            return mean;
        }
        float sum = 0;
        for(float a : data)
            sum += a;
        return sum/size;
    }

    float getVariance() {
        if(variance != 0){
            return variance;
        }
        float mean = getMean();
        float temp = 0;
        for(float a :data)
            temp += (a-mean)*(a-mean);
        return temp/(size-1);
    }

    float getStdDev() {
        if(std != 0){
            return std;
        }
        return (float) Math.sqrt(getVariance());
    }

}