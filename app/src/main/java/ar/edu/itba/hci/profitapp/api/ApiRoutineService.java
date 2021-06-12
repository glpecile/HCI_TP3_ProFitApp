package ar.edu.itba.hci.profitapp.api;

import androidx.lifecycle.LiveData;

import ar.edu.itba.hci.profitapp.api.model.PagedList;
import ar.edu.itba.hci.profitapp.api.model.Routine;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ApiRoutineService {
    @GET("routines")
    LiveData<ApiResponse<PagedList<Routine>>> getRoutines();

    @GET("routine/{routineId}")
    LiveData<ApiResponse<Routine>> getRoutine(@Path("routineId") int routineId);
}