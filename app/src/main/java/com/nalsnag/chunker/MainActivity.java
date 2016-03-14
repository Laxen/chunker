package com.nalsnag.chunker;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements RecyclerView.OnTouchListener {
    private RecyclerView recyclerView;
    private RecyclerAdapter recyclerAdapter;
    private RecyclerView.LayoutManager recyclerLayoutManager;

    private ArrayList<Task> taskList;

    private OnGestureListener gestureListener;
    private GestureDetector gestureDetector;

    private SharedPreferences sp;
    private SharedPreferences.Editor spEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpTaskList();
        setUpRecyclerView();
        setUpGestureDetector();
        setUpFAB();
    }

    private Task loadTask(String baseTaskIdentifier, int taskIdentifier) {
        String taskName = sp.getString(baseTaskIdentifier + taskIdentifier + "NAME", "ERROR: TASK NAME COULD NOT BE LOADED");
        boolean done = sp.getBoolean(baseTaskIdentifier + taskIdentifier + "DONE", false);
        Task task = new Task(taskName, done);

        int nSubTasks = sp.getInt(baseTaskIdentifier + taskIdentifier + "NUMBER_OF_SUB_TASKS", 0);
        for(int i = 0; i < nSubTasks; i++) {
            String subBaseTaskIdentifier = baseTaskIdentifier + taskIdentifier;
            int subTaskIdentifier = i;
            Task subTask = loadTask(subBaseTaskIdentifier, subTaskIdentifier);

            task.addSubTask(subTask);
        }

        return task;
    }

    private void saveTask(Task task, String baseTaskIdentifier, int taskIdentifier) {
        String taskName = task.getTaskName();
        boolean done = task.isDone();
        int nSubTasks = task.getSubTasks().size();
        spEditor.putString(baseTaskIdentifier + taskIdentifier + "NAME", taskName);
        spEditor.putBoolean(baseTaskIdentifier + taskIdentifier + "DONE", done);
        spEditor.putInt(baseTaskIdentifier + taskIdentifier + "NUMBER_OF_SUB_TASKS", nSubTasks);

        for(int i = 0; i < nSubTasks; i++) {
            Task newTask = task.getSubTasks().get(i);
            String newBaseTaskIdentifier = baseTaskIdentifier + taskIdentifier;
            int newTaskIdentifier = i;

            saveTask(newTask, newBaseTaskIdentifier, newTaskIdentifier);
        }

        /**
         * 1. Have task, baseTaskIdentifier (String) and taskIdentifier (int)
         * 2. Save task name
         * 3. Save done status
         * 4. Save number of sub tasks
         * 5. For each sub task
         *      5a. Copy baseTaskIdentifier and taskIdentifier to new variables, work with these below
         *      5b. Add taskIdentifier to end of baseTaskIdentifier
         *      5c. Set taskIdentifier to the sub task number (i in the for loop)
         *      5d. Call method again with new identifiers
         *
         * (No return as in loadTask)
         */
    }

    private void setUpTaskList() {
        taskList = new ArrayList<>();

        sp = getPreferences(MODE_PRIVATE);
        int mainListSize = sp.getInt("MAIN_LIST_SIZE", 0);

        for(int i = 0; i < mainListSize; i++) {
            String baseTaskIdentifier = "Task";
            int taskIdentifier = i;
            Task task = loadTask(baseTaskIdentifier, taskIdentifier);

            task.setContainerList(taskList);
            taskList.add(task);
        }

        /**
         * 1. Get initial number of tasks (main tasks)
         * 2. For each task
         *      2a. baseTaskIdentifier = "Task";
         *      2b. taskIdentifier = i;
         *      2c. loadTask(parameters)
         *      2d. Add returned task to taskList
         *
         * loadTask()
         * 1. Have baseTaskIdentifier (String) and taskIdentifier (int)
         * 1.5. Get task name
         * 2. Get done status
         * 3. Create task
         * 4. Get number of sub tasks
         * 5. For each sub task
         *      5a. Copy baseTaskIdentifier and taskIdentifier to new variables, work with these below
         *      5b. Add taskIdentifier to end of baseTaskIdentifier
         *      5c. Set taskIdentifier to the sub task number (i in the for loop)
         *      5d. Call method again with new identifiers
         *      5e. Add returned task to sub task list
         * 6. Return task
         */

//        Task uwot = new Task("u wot");
//        uwot.setContainerList(taskList); // uwot is contained in taskList
//            Task m8 = new Task("m8");
//            Task m9 = new Task("m9");
//            uwot.addSubTask(m8); // This makes sure that m8 is contained in uwot subTasks and has uwot as masterTask
//            uwot.addSubTask(m9);
//        Task rekt = new Task("rekt");
//        rekt.setContainerList(taskList);
//
//        taskList.add(uwot);
//        taskList.add(rekt);
//
//        for(int i = 0; i < 20; i++) {
//            Task task = new Task("Task " + i);
//            task.setContainerList(taskList);
//                Task subTask = new Task("SubTask " + i);
//                task.addSubTask(subTask);
//            taskList.add(task);
//        }
    }

    private void setUpRecyclerView() {
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        recyclerLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(recyclerLayoutManager);

        recyclerAdapter = new RecyclerAdapter(taskList);
        recyclerView.setAdapter(recyclerAdapter);

        //recyclerView.setOnTouchListener(this);
    }

    private void setUpGestureDetector() {
//        gestureListener = new OnGestureListener(recyclerAdapter);
//        gestureDetector = new GestureDetector(gestureListener);
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(recyclerAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void setUpFAB() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {

            /**
             * Code to add task
             */
            @Override
            public void onClick(View v) {
                recyclerAdapter.addNewTask();
            }
        });
    }

    /**
     * Changes the displayed task list to the previous one
     */
    @Override
    public void onBackPressed() {
        ArrayList<Task> newTaskList = recyclerAdapter.getMasterTaskList(); // Gets the master task list which should become the current task list

        if(newTaskList == null) { // There is no master task list (we are on the main tasks), save and then exit app
            // THIS SHOULD BE IN THE METHOD CALLED WHEN THE TASK QUITS

            spEditor = sp.edit();
            spEditor.putInt("MAIN_LIST_SIZE", taskList.size());

            for(int i = 0; i < taskList.size(); i++) {
                saveTask(taskList.get(i), "Task", i);
            }

            spEditor.apply();

            super.onBackPressed();
            return;
        }

        Task task = newTaskList.get(0); // Gets the first task of the master task list (this will ALWAYS exist)
        Task masterTask = task.getMasterTask(); // Gets the master task of the master task list

        ArrayList<Task> newMasterTaskList; // Create an ArrayList to hold the new master task list
        if(masterTask == null) { // There is no master task, "task" is in the first task list
            newMasterTaskList = null;
        } else {
            newMasterTaskList = masterTask.getContainerList(); // Gets the container list of the MASTER TASK for "task"
        }

        recyclerAdapter.setMasterTask(recyclerAdapter.getMasterTask().getMasterTask()); // Sets the master task to the current master tasks master task

        ArrayList<Task> currentTaskList = recyclerAdapter.getTaskList(); // Gets the CURRENT task list (only used to get the animations working properly)

        recyclerAdapter.setMasterTaskList(newMasterTaskList); // Sets master task list
        recyclerAdapter.setTaskList(newTaskList); // Sets current task list

        // Notification is done this way to get nice fade out animations
        recyclerAdapter.notifyItemRangeRemoved(0, currentTaskList.size());
        recyclerAdapter.notifyItemRangeInserted(0, newTaskList.size());
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        View view = recyclerView.findChildViewUnder(event.getX(), event.getY());
        int position = recyclerView.getChildAdapterPosition(view);
        gestureListener.setView(view);
        gestureListener.setPosition(position);
        gestureDetector.onTouchEvent(event);

        // ALL TOUCH EVENTS ARE IN OnGestureListener CLASS!!

        return false;
    }

    /**
     * TODO
     * OnGestureListener is not used, neither is onTouch on RecyclerView
     *
     * CLEAN UP LOAD AND SAVE
     *      PUT SAVE IN METHOD BEFORE APP IS CLOSED
     *
     *
     *
     *
     *
     *
     *
     *
     *
     * I think containerList is unnecessary
     *      Could be done with just masterTask.getSubTasks() ?
     *
     * Maybe switch to CardView to get z level when moving?
     *
     * Make line pretty with correct width
     *      Maybe just add a view that blocks the line from showing after task_title
     *      Or do it properly by getting task_title layout params and making sure line does not get wider than task_title
     *
     * Make completed task gray out (include line?)
     *
     * Maybe skip task description and just go with title
     *      Make title font smaller to fit more text
     */
}
