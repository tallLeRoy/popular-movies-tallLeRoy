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
 }
