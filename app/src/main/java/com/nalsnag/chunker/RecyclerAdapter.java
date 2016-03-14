package com.nalsnag.chunker;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
    private ArrayList<Task> taskList; // The current list of tasks that are displayed
    private ArrayList<Task> masterTaskList; // The master task list of the currently displayed task list, null if on the first tasks
    private Task masterTask; // The task we clicked to enter the current task list (used when adding new tasks since we need to know the master task)

        /**
         * ViewHolder holds all views for an element in taskList
         */
        public static class ViewHolder extends RecyclerView.ViewHolder {
            private EditTextBackHandler titleView; // The view for the title of the task
            private TextView description;
            private ImageView editView; // The view for editing (the little pen)
//            private TableRow line; // The line that gets drawn when swiping a task
            private Task task; // The actual task object. Contains the sub tasks

            public ViewHolder(View v) {
                super(v);
                titleView = (EditTextBackHandler) v.findViewById(R.id.task_title);
                description = (TextView) v.findViewById(R.id.task_desc);
                editView = (ImageView) v.findViewById(R.id.task_edit);
//                line = (TableRow) v.findViewById(R.id.task_line);

                titleView.setViewHolder(this); // The title view needs a reference to the ViewHolder because it calls updateTask() (used for soft keyboard)
            }

            /**
             * Changes the view to another task (changing task title and making sure the line is correct)
             */
            public void reset() {
                titleView.setText(task.getTaskName()); // Changes the text of the task
                description.setText(task.getTaskDescription()); // Changes the description of the task

                if(task.isDone()) {
                    titleView.setTextColor(Color.GRAY);
                    titleView.setPaintFlags(titleView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                } else {
                    titleView.setTextColor(Color.BLACK);
                    titleView.setPaintFlags(titleView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                }

//                boolean done = task.isDone();
//                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) line.getLayoutParams();
//
//                if(done) {
//                    params.width = RelativeLayout.LayoutParams.MATCH_PARENT; // Makes the task dashed through
//                    line.setVisibility(TableRow.VISIBLE);
//                } else {
//                    params.width = 0; // Removes the line
//                    line.setVisibility(TableRow.INVISIBLE);
//                }
//
//                line.setLayoutParams(params);
            }

            /**
             * Changes the task to match what is displayed in the list
             * Called after a task has been edited to save the edit
             */
            public void updateTask() {
                task.setTaskName(titleView.getText().toString());
            }

            public void setTask(Task task) {
                this.task = task;
            }
        }

    public RecyclerAdapter(ArrayList<Task> taskList) {
        this.taskList = taskList;
    }

    /**
     * Switches out the current taskList for the taskList of the view supplied
     */
    private void enterTask(ViewHolder vh) {
        int position = vh.getAdapterPosition(); // Gets the position of the view we clicked
        Task temp = taskList.get(position); // Gets the task in that position
        ArrayList<Task> newTaskList = temp.getSubTasks(); // Gets the list of sub tasks for that task

        int currentTaskListSize = taskList.size(); // Get the number of tasks in the current list
        masterTask = temp; // Sets the master task to be the task we clicked
        masterTaskList = taskList; // Sets the master task list to be the currently displayed task list
        taskList = newTaskList; // Changes to the new task list

        // This is needed to get the nice fade out/in animation when a task is clicked
        notifyItemRangeRemoved(0, currentTaskListSize); // Notify removal of all the tasks in the current list
        notifyItemRangeInserted(0, taskList.size()); // Notify the addition of all the sub tasks for the clicked task
    }

    /**
     * Creates new views (invoked by the layout manager)
     */
    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_view, parent, false); // Inflate the task view layout
        final ViewHolder vh = new ViewHolder(v); // Make a ViewHolder for this task

        /**
         * Adds a click listener for the view (which is NEVER removed, only the task it displayed is changed)
         */
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("HEJ", "VIEW");

                enterTask(vh); // Changes the taskList to the sub task list of the task clicked on
            }
        });

        /**
         * Adds a click listener for edit/pen view
         */
        vh.editView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("HEJ", "EDIT");

                enterTaskEditMode(vh);
            }
        });

        /**
         * Adds a click listener for the title view
         * This is needed because otherwise EditText consumes the touch without action
         */
        vh.titleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!v.isFocusableInTouchMode()) {
                    enterTask(vh); // Enter the task if we are not editing the title
                }
            }
        });

        /**
         * Adds an editor action listener
         * Handles click on the "done" button on the soft keyboard
         */
        vh.titleView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE) { // If we click on done button
                    Log.d("HEJ", "DONE");
                    vh.titleView.setFocusableInTouchMode(false); // Make titleView unfocusable by touch
                    vh.titleView.clearFocus(); // And clear its focus

                    vh.updateTask(); // Saves the changes to the task object

                    // Hide soft keyboard
                    final InputMethodManager inputMethodManager = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(vh.titleView.getWindowToken(), 0);

                    // THE HANDLER FOR WHEN SOFT KEYBOARD IS DISMISSED BY BACK KEY IS FOUND IN EditTextBackHandler CLASS
                }
                return false;
            }
        });

        return vh;
    }

    /**
     * Makes the EditText editable and shows the soft keyboard
     */
    private void enterTaskEditMode(ViewHolder vh) {
        vh.titleView.setFocusableInTouchMode(true); // Make the title view focusable by touch
        vh.titleView.requestFocus(); // Focus it

        // Show soft keyboard
        final InputMethodManager inputMethodManager = (InputMethodManager) vh.titleView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(vh.titleView, InputMethodManager.SHOW_IMPLICIT);
    }

    /**
     * Adds a new task and enters edit mode (not implemented)
     */
    public void addNewTask() {
        Task task = new Task("New task");
        task.setContainerList(taskList);
        task.setMasterTask(masterTask);

        for(int i = 0; i < taskList.size(); i++) {
            Task temp = taskList.get(i);
            if(temp.isDone()) {
                taskList.add(i, task);
                notifyItemInserted(i);
                break;
            }
        }

        // ADD CODE TO ENTER EDIT MODE
    }

    /**
     * Replace the contents of a view (invoked by the layout manager)
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.setTask(task); // Changes the task in the view holder
        holder.reset(); // Resets the view holder so it displays the right task data
    }

    /**
     * Return the size of the dataset (invoked by the layout manager)
     */
    @Override
    public int getItemCount() {
        return taskList.size();
    }

    /**
     * Returns the current taskList
     */
    public ArrayList<Task> getTaskList() {
        return taskList;
    }

    public ArrayList<Task> getMasterTaskList() {
        return masterTaskList;
    }

    /**
     * Sets the taskList which should be displayed right now (need to notify adapter to actually see a difference)
     */
    public void setTaskList(ArrayList<Task> taskList) {
        this.taskList = taskList;
    }

    public void setMasterTaskList(ArrayList<Task> masterTaskList) {
        this.masterTaskList = masterTaskList;
    }

    public Task getMasterTask() {
        return masterTask;
    }

    public void setMasterTask(Task masterTask) {
        this.masterTask = masterTask;
    }

    /**
     * This is used in OnGestureListener, which is probably no longer needed
     */
    public void onClick(int position) {
        Task temp = taskList.get(position);
        ArrayList<Task> newTaskList = temp.getSubTasks(); // Gets the list of subtasks for that task

        if(newTaskList != null) { // If there are subtasks
            int currentTaskListSize = taskList.size();
            taskList = temp.getSubTasks();
            notifyItemRangeRemoved(0, currentTaskListSize);
            notifyItemRangeInserted(0, taskList.size());
        } else {
            //Toast.makeText(null, "No subtasks!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Called from SimpleItemTouchHelperCallback when item is swiped completely (NOT during swipe)
     */
    public void onItemDismiss(ViewHolder viewHolder) {
        int pos = viewHolder.getAdapterPosition();
        Task task = taskList.get(pos);

        taskList.remove(pos);
        notifyItemRemoved(pos);

        if(!task.isDone()) {
            task.setDone(true);
            taskList.add(taskList.size(), task);
            notifyItemInserted(taskList.size());
        }
    }

    /**
     * Called from SimpleItemTouchHelperCallback when item is moved completely (NOT during move)
     * I have not looked through this code, it has been copied from StackOverflow
     */
    public boolean onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(taskList, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(taskList, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    /**
     * Called from SimpleItemTouchHelperCallback when item is being swiped and animation should be drawn (DURING swipe)
     * This is actually in the draw loop, like it should be
     */
//    public void onSwiping(ViewHolder viewHolder, float dX) {
//        viewHolder.animateSwipe(dX);
//    }
}
