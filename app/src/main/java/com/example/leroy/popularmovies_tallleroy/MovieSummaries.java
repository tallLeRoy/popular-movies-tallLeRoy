package com.example.leroy.popularmovies_tallleroy;

import java.util.List;
/**
 * Created by LeRoy on 8/10/2015.
 */
public class MovieSummaries {
    static List<MovieSummary> movieSummaries = null;

    public MovieSummaries() {
    }

    static List<MovieSummary> getMovieSummaries() {
        return movieSummaries;
    }

    static void setMovieSummaries(List<MovieSummary> movieSummariesIn) {
        movieSummaries = movieSummariesIn;
    }

    static long popularityComparator(MovieSummary ms1, MovieSummary ms2) {
        Double d1 = Double.parseDouble(ms1.getPopularity());
        Double d2 = Double.parseDouble(ms2.getPopularity());

        return d1.compareTo(d2);
    }

    static long ratingComparator(MovieSummary ms1, MovieSummary ms2) {
        Double d1 = Double.parseDouble(ms1.getVote_average());
        Double d2 = Double.parseDouble(ms2.getVote_average());

        return d1.compareTo(d2);
    }
}
