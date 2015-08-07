package com.example.leroy.popularmovies_tallleroy;

import java.util.List;

/**
 * Created by LeRoy on 8/7/2015.
 *
 */

public interface AsyncSupplyBitmapResponse {
    void bitmapsAvailable(List<MovieSummary> movieSummaries);
}
