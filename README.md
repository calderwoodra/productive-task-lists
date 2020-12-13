## Description
Do you have a lot of work that comes in at random times and needs to be completed
by different deadlines? Have you tried Google tasks? Yea, I have to, it doesn't really
scale for people with a ton of shit to do.

Productive Tasks organizes your tasks by deadlines and categories. Categories are organized like
a computer directory structure:
```
-- root
 | -- personal
    | -- groceries
    | -- chores
    | -- personal projects
       | -- productive tasks app
       | -- startup
    | -- vacation ideas
 | -- work
    | -- current projects
       | -- project 1
       | -- project 2
    | -- old projects
       | -- project 3
       | -- project 4
          | -- feature 1
          | -- feature 2
       | -- project 5
```

Each category/directory can have more directories and/or some tasks listed inside it.

Tasks generally have deadlines associated with them. For the tasks that don't have deadlines,
productive tasks offers a way to sort tasks such that the highest priority ones appear in
your "Top Tasks" list.

This project is still a work in progress and accepting PRs (10/13/2020). See below for available
tasks and project setup.

## Setup
1. Install Android Studio 4.1 or higher
2. Import project
3. Install plugins (save actions, google-java-format, etc.)
4. Import my style guide
    1. [Intellij Style Guide](https://github.com/calderwoodra/intellij-java-style-guides/blob/master/GJF.xml)
    2. [Save actions config](https://github.com/calderwoodra/intellij-java-style-guides/blob/master/Screen%20Shot%202020-10-15%20at%2012.48.55%20AM.png)
5. Install commit hooks:
    1. `$ brew install pre-commit`
    2. `$ cd ~/path/to/repo`
    3. `$ pre-commit install`
6. `$ ./gradlew installDebug` or build in Android Studio

## Things left to do (in no particular order)
### Tasks
1. ~~add room database to save tasks~~
2. ~~allow users to mark tasks complete~~
3. ~~allow users to edit tasks~~
4. ~~allow users to set deadlines and reminders~~
5. ~~allow users to set repeatable reminders~~
6. ~~sort tasks based on deadline~~

### Directories
1. ~~allow users to select a directory~~
2. ~~allow users to browse directories and tasks~~
3. ~~allow users to create directories~~
4. ~~allow users to rename directories (long click)~~

### Launch
1. ~~push notifications and reminders~~
2. Deploy to play store
3. Add rate app dialog

### Stretch
1. allow users to delete directories (move tasks to home or delete all tasks as well)
2. allow users to move directories
3. add empty rv state
4. add tutorials
5. add user login
6. save tasks on the server
7. implement sorting/prioritizing feature (directories, tasks, etc.)
8. Implement data dashboard (tasks completed this week, this month, all time).
9. Add demo gifs to README
10. allow users to purchase the app
11. update app theme
