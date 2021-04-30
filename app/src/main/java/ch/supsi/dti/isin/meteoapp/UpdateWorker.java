package ch.supsi.dti.isin.meteoapp;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutionException;

import ch.supsi.dti.isin.meteoapp.model.Location;
import ch.supsi.dti.isin.meteoapp.model.LocationsHolder;
import ch.supsi.dti.isin.meteoapp.tasks.UpdateLocationInfoTask;

public class UpdateWorker extends Worker {

    private static final double TEMP_MIN =  20;
    private static final double TEMP_MAX =  30;

    public UpdateWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {

        UpdateLocationInfoTask updateLocationInfoTask = new UpdateLocationInfoTask();
        try {

            List<Location> updatedList = LocationsHolder.get(getApplicationContext()).getLocations();
            updateLocationInfoTask.execute(updatedList).get();


            int counter = 0;
            for (Location location : updatedList){
                StringBuilder stringBuilder = new StringBuilder();

                if(location.getTemp_min() < TEMP_MIN)
                    stringBuilder.append(location.getName().toUpperCase() + " :\n " + "min_temp = " + location.getTemp_min() + "\n");

                if(location.getTemp_min() > TEMP_MAX)
                    stringBuilder.append(location.getName().toUpperCase() + " :\n " + "max_temp = " + location.getTemp_max() + "\n");

                if(stringBuilder.length() > 0) {
                    NotificationCompat.Builder mBuilder = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        mBuilder = new NotificationCompat.Builder(getApplicationContext(), "default")
                                .setSmallIcon(android.R.drawable.ic_menu_info_details)
                                .setContentTitle(LocalTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME))
                                .setContentText(stringBuilder.toString())
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                    }

                    NotificationManagerCompat managerCompat = NotificationManagerCompat.from(getApplicationContext());
                    managerCompat.notify(counter, mBuilder.build());
                    counter++;
                }
            }
            return Result.success();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        return Result.failure();
    }
}
