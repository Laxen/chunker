package com.nalsnag.chunker;

import java.util.ArrayList;

public class Task {
    private String taskName;
    private ArrayList<Task> subTasks; // Holds the list that the subtasks for the current task is contained in
    private ArrayList<Task> containerList; // Holds the list that the current task is contained in
    private Task masterTask; // Holds the master task for this task

    private boolean done = false;

    public Task(String taskName) {
        this.taskName = taskName;
        this.subTasks = new ArrayList<>();
        this.masterTask = null;
    }

    public Task(String taskName, boolean done) {
        this(taskName);
        this.done = done;
    }

    public String getTaskName() {
        return taskName;
    }

    public void addSubTask(Task task) {
        task.setContainerList(subTasks);
        task.setMasterTask(this);
        subTasks.add(task);
    }

    public ArrayList<Task> getSubTasks() {
//        if(subTasks.isEmpty()) {
//            return null;
//        } else {
//            return subTasks;
//        }

        return subTasks;
    }

    public void setContainerList(ArrayList<Task> containerList) {
        this.containerList = containerList;
    }

    /**
     * Can this not be replaced with masterTask.getSubTasks() ?
     */
    public ArrayList<Task> getContainerList() {
        return containerList;
    }

    public void setMasterTask(Task task) {
        masterTask = task;
    }

    public Task getMasterTask() {
        return masterTask;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public boolean isDone() {
        return done;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskDescription() {
        String masterTaskName = "null";
        if(masterTask != null) {
            masterTaskName = masterTask.getTaskName();
        }
        return "Subtasks: " + subTasks.size() + " - masterTask = " + masterTaskName + " - Done = " + done;
    }
}
