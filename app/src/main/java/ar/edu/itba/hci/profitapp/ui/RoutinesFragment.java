package ar.edu.itba.hci.profitapp.ui;

import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Locale;
import java.util.stream.Collectors;

import ar.edu.itba.hci.profitapp.App;
import ar.edu.itba.hci.profitapp.R;
import ar.edu.itba.hci.profitapp.api.model.Routine;
import ar.edu.itba.hci.profitapp.databinding.FragmentHomeBinding;
import ar.edu.itba.hci.profitapp.databinding.FragmentRoutinesBinding;
import ar.edu.itba.hci.profitapp.repository.RoutineRepository;
import ar.edu.itba.hci.profitapp.repository.Status;
import ar.edu.itba.hci.profitapp.ui.adapters.RoutinesCustomAdapter;
import ar.edu.itba.hci.profitapp.viewModel.FavoritesViewModel;
import ar.edu.itba.hci.profitapp.viewModel.RoutineViewModel;
import ar.edu.itba.hci.profitapp.viewModel.repositoryVM.RepositoryViewModelFactory;

public class RoutinesFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    private App app;
    private FragmentRoutinesBinding fragmentRoutinesBinding;
    private RoutinesCustomAdapter routinesAdapter;
    private RecyclerView recyclerView;
    private RoutineViewModel routineViewModel;
    private FavoritesViewModel favoritesViewModel;

    private final static int PAGE_SIZE = 10;

    private int routinePage = 0;
    private String orderBy = "date";
    private String direction = "asc";
    private boolean isLastRoutinePage = false;

    public RoutinesFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentRoutinesBinding = FragmentRoutinesBinding.inflate(getLayoutInflater());

        recyclerView = fragmentRoutinesBinding.routinesRecyclerView;

        return fragmentRoutinesBinding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Spinner spinner = fragmentRoutinesBinding.routinesSpinner;
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this.getContext(), R.array.order_by_options, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(this);


        app = ((App) getActivity().getApplication());

        ViewModelProvider.Factory viewModelFactory = new RepositoryViewModelFactory<>(RoutineRepository.class, app.getRoutineRepository());
        routineViewModel = new ViewModelProvider(this, viewModelFactory).get(RoutineViewModel.class);

        ViewModelProvider.Factory favoriteViewModelFactory = new RepositoryViewModelFactory<>(RoutineRepository.class, app.getRoutineRepository());
        favoritesViewModel = new ViewModelProvider(this, favoriteViewModelFactory).get(FavoritesViewModel.class);

        //TODO hay un error que no termina llamando a la api. Corregir
        View.OnClickListener favoriteClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Routine routine = (Routine) v.getTag();
                Log.d("TAG", Integer.toString(routine.getId()));
                if(routine.getFavorite()) {
                    app.getRoutineRepository().addFavorite(routine.getId()).observe(getViewLifecycleOwner(), r->{});

                } else {
                    app.getRoutineRepository().deleteFavorite(routine.getId()).observe(getViewLifecycleOwner(), r->{});
                }
            }
        };

        routinesAdapter = new RoutinesCustomAdapter(new ArrayList<>(), favoriteClickListener);

        recyclerView.setAdapter(routinesAdapter);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        }
        recyclerView.setHasFixedSize(true);

//        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//        });

        getRoutines();
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        String orderByStr = parent.getItemAtPosition(pos).toString();
        if(Locale.getDefault().getLanguage().equals("en")) {
            switch (orderByStr) {
                case "Category": orderByStr = "categoryId"; break;
                case "Date": orderByStr = "date"; break;
                case "Difficulty": orderByStr = "difficulty"; break;
                case "Rating": orderByStr = "averageRating"; break;
            }
        } else if(Locale.getDefault().getLanguage().equals("es")) {
            switch (orderByStr) {
                case "Categoría": orderByStr = "categoryId"; break;
                case "Fecha": orderByStr = "date"; break;
                case "Dificultad": orderByStr = "difficulty"; break;
                case "Puntuación": orderByStr = "averageRating"; break;
            }
        }
        orderBy = orderByStr;
        routinesAdapter.clearRoutines();
        routinesAdapter.notifyDataSetChanged();
        getRoutines();

        Log.d("ORDER", orderBy);
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    public void getRoutines() {
        app.getRoutineRepository().getRoutines(routinePage, PAGE_SIZE, orderBy, direction).observe(getViewLifecycleOwner(), r -> {
            if (r.getStatus() == Status.SUCCESS) {
                if (r.getData() != null && r.getData().getContent() != null) {
                    favoritesViewModel.getFavorites().observe(getViewLifecycleOwner(), favRes -> {
                        if (favRes.getStatus() == Status.SUCCESS) {
                            if (favRes.getData() != null && favRes.getData().getContent() != null) {
                                r.getData().getContent().forEach(routine -> {
                                    if (favRes.getData().getContent().stream().map(Routine::getId).collect(Collectors.toList()).contains(routine.getId())) {
                                        routine.setFavorite(true);
                                    }
                                });
                                routinesAdapter.addRoutines(r.getData().getContent());
                                routinesAdapter.notifyDataSetChanged();
                            }
                        } else {

                        }
                    });
                }
            } else {
//                defaultResourceHandler(r);
            }
        });
    }
}