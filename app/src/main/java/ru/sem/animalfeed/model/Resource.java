package ru.sem.animalfeed.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Resource<T> {
    @NonNull
    public final ResourceStatus status;

    @Nullable
    public final T data;

    @Nullable public final String message;


    private Resource(@NonNull ResourceStatus status, @Nullable T data, @Nullable String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }

    public static <T> Resource<T> success(@NonNull T data) {
        return new Resource<>(ResourceStatus.SUCCESS, data, null);
    }

    public static <T> Resource<T> error(String msg, @Nullable T data) {
        return new Resource<>(ResourceStatus.ERROR, data, msg);
    }


    public static <T> Resource<T> loading(@Nullable T data) {
        return new Resource<>(ResourceStatus.LOADING, data, null);
    }

    public boolean isSuccess() {
        return status == ResourceStatus.SUCCESS && data != null;
    }

    public boolean isLoading() {
        return status == ResourceStatus.LOADING;
    }

    public boolean isLoaded() {
        return status != ResourceStatus.LOADING;
    }
}
