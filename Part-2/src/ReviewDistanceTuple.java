/**
 * Created by mpokr on 4/22/2017.
 */
public class ReviewDistanceTuple implements Comparable {

    private MovieReview originReview;
    private MovieReview destinationReview;
    private double distance;

    public ReviewDistanceTuple(MovieReview originReview, MovieReview destinationReview, double distance) {
        this.originReview = originReview;
        this.destinationReview = destinationReview;
        this.distance = distance;
    }


    public MovieReview getOriginReview() {
        return originReview;
    }

    public MovieReview getDestinationReview() {
        return destinationReview;
    }

    public double getDistance() {
        return distance;
    }

    public int compareTo(Object otherTuple) {
        if (!(otherTuple instanceof ReviewDistanceTuple)) {
            throw new IllegalArgumentException("ReviewDistanceTuple object expected. Got " + otherTuple.getClass());
        }  else {
            double otherTupleDistance = ((ReviewDistanceTuple) otherTuple).getDistance();
            double result = this.distance - otherTupleDistance;
            if (result > 0) {
                return 1;
            } else if (result < 0) {
                return -1;
            } else {
                return 0;
            }
        }
    }


}
