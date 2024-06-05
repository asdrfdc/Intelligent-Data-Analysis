package com.zm.bi.manager;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

@Component
public class TaskManager {

    private final Map<String, Future<?>> taskFutureMap = new ConcurrentHashMap<>();

    public void addTask(String taskId, Future<?> future) {
        taskFutureMap.put(taskId, future);
    }

    public Future<?> getTask(String taskId) {
        return taskFutureMap.get(taskId);
    }

    public Future<?> removeTask(String taskId) {
        return taskFutureMap.remove(taskId);
    }
}
