// We are the sole authors of the work in this repository.

import java.util.Scanner;
import structure5.*;
import java.util.Iterator;

/**
 * Class to create graph representing students and courses from text file
 * and then create a suitable exam schedule without conflicts. Also printing
 * some useful information about exam schedule per course and per student
 */
public class ExamScheduler {

  // current max number slot for exams increments when needed
  protected static int max = 1;

  /**
   * Method to generate a graph from text file representing edges and vertices.
   * Courses are considered vertices and students are edges connecting vetrices
   * of course that they are taking. If two students are taking the same two
   * courses they will both be present in the edge label. Courses are represented
   * by associations, the key is the course name and value will be set when the
   * exam schedule is set. It will be the slot that the course will be taken
   * in the exam period. Students are a class which contains a vector of courses
   * which are the courses they are taking. This method takes all this into
   * account when creating a graph representing the data form the file.
   *
   * @param g Emtpy graph to be populated in this method
   * @param s instance of scanner object taking input from command line
   */
  public static void generateGraph(Graph<Association<String, Integer>, Vector<Student>> g, Scanner s) {

    // while there are still students and courses to add
    while (s.hasNextLine())  {

      // create a new student from the first line
      Student stu = new Student(s.nextLine());

      // the next lines are going to be courses the student is taking
      // create associtaions to represent vertices
      for (int i = 0; i < 4; i++) {
        createCourse(s.nextLine(), stu, g);
      }

      // create edges between to vertices
      for (Association<String, Integer> c1 : stu.getCourses()) {
        for (Association<String, Integer> c2 : stu.getCourses()) {
          // dont make any loops, as long as we have two distinct vertices create an edge
          if (!c1.equals(c2)) {
            // handle the logic for creating or just adding a student to an edge
            addEdge(c1, c2, stu, g);
          }
        }
      }
    }
  }

  /**
   * Helper method to handle logic of creating an edge between two unique vertices
   * If the edge is already created then the student that was just created needs to be
   * added to the label of the edge to show that they are ALSO taking both those classes
   * If the edge has not been created then create the label (a vector) add the student
   * to the label and then create an edge between the two vertices.
   *
   * @pre !v1.equals(v2)
   * @post a new edge is created or a student is in an edge label
   * @param v1 course which is a vertex
   * @param v2 another course which is the other vertex
   * @param student student taking those courses
   * @param g the graph
   */
  protected static void addEdge(
    Association<String, Integer> v1,
    Association<String, Integer> v2,
    Student student,
    Graph<Association<String, Integer>, Vector<Student>> g
  ) {
    // vertices should not be equal
    Assert.pre(!v1.equals(v2), "This graph is a simple graph, edges are between two unique vertices");

    // get the edge, it is possible it does not exist, if so it will be null
    // note edge btw v1-v2 is the same as v2-v1
    Edge<Association<String, Integer>, Vector<Student>> edge = g.getEdge(v1, v2);

    // check if we need to create an edge or just change the label of the existing edge
    if (edge == null) {
      // create a new edge, here is the new label
      Vector<Student> v = new Vector<>();
      // add student to the label
      v.add(student);
      // create the edge between the vertices
      g.addEdge(v1, v2, v);
    } else {
      // the edge already exists
      // if the student is not already in the edge label
      if (!edge.label().contains(student)) {
        // add the student to the edge label
        edge.label().add(student);
      }
    }
  }

  /**
   * Helper method for creating a course/vertex. Vertices are associations, the key
   * is the course name and the value is the to be slot of its final schedule. If the course
   * has not been given a slot (ie is has just been created) its value will be -1. When a vertex
   * is created it should also be added to the students courses field in the student object.
   *
   * @pre courseName is something as well as student
   * @post vertex is either created or found
   * @param courseName string course name
   * @param student student taking this course
   * @param g graph to be updated
   */
  protected static void createCourse(
    String courseName,
    Student student,
    Graph<Association<String, Integer>, Vector<Student>> g
  ) {
    // pre condition
    Assert.pre(courseName != null && student != null, "Student and course must not be null");

    // create the association
    Association<String, Integer> course = new Association<>(courseName, -1);

    // check if the graph already contains this 'vertex'
    if (g.contains(course)) {
      // get that already created vertex to not double add vertices
      Association<String, Integer> c = g.get(course);
      // add that object to students courses so when one is updated its just a reference to the same course
      student.addCourse(c);
    } else {
      // graph does not contain the course so add it as a vertex
      g.add(course);
      // add course to students courses
      student.addCourse(course);
    }
  }

  /**
  * Method to create the list of time slots with the courses whose final will
  * be given at that slot such that no pair of time slots can be combined without
  * creating a time conflict with a student.
  *
  * @param g the graph
  * @post an efficient exam schedule has been created without conflicts
  **/
  public static void makeSchedule(Graph<Association<String, Integer>, Vector<Student>> g) {

    // iterator for vertices (courses) in graph g
    Iterator<Association<String, Integer>> it = g.iterator();

    // pass the vertices (courses) into the helper function
    while (it.hasNext()) {
      makeScheduleHelper(g, it.next());
    }
  }

  /**
  * Helper method to create the list of time slots. Vertices are associations,
  * the key is the course name and the value is the to be slot of its final
  * schedule. If the course has not been given a slot (ie is has just been created)
  * its value will be -1. Int max represents the current max number of slots.
  *
  * @param g the graph
  * @param c the course
  **/
  public static void makeScheduleHelper(
    Graph<Association<String, Integer>,
    Vector<Student>> g,
    Association<String, Integer> c
  ) {

    // vector representing the time slots
    Vector<Integer> timeSlots = new Vector<>();

    // iterator for neighbors of course c
    Iterator<Association<String, Integer>> it = g.neighbors(c);

    // for all neighbors (courses) of c, add their time to timeSlots
    while (it.hasNext()) {
      timeSlots.add(it.next().getValue());
    }

    // set c to the next available time slot
    for (int i = 0; i < max; i++) {
      if (!timeSlots.contains(i)) {
        c.setValue(i);
      }
    }

    // if no more available slots, set c to the max available time slot and
    // increment the max number of slots
    if (c.getValue() == -1) {
      c.setValue(max);
      max++;
    }

    // reset the iterator
    it = g.neighbors(c);

    // call this method on any courses that have not yet been alloted a slot
    while (it.hasNext()) {
      Association<String, Integer> course = it.next();
      if (course.getValue() == -1) {
        makeScheduleHelper(g, course);
      }
    }
  }

  /**
  * Method to print the exam schedule. This consists of the time slot and a list
  * of the courses at each time slot.
  *
  * @param g the graph
  * @post the exam schedule is printed
  **/
  public static void printSchedule(Graph<Association<String, Integer>, Vector<Student>> g) {

    // slots Vector stores the courses at each slot
    Vector<Vector<Association<String, Integer>>> slots = new Vector<>();
    slots.setSize(max);

    // call function to allot each course to its time slot
    slots = allotSlots(g, slots);

    // print the time slots and their courses
    int i = 1;
    for (Vector<Association<String, Integer>> slot : slots) {
      System.out.print("Slot " + i + ": ");
      for (Association<String, Integer> c : slot) {
        System.out.print(c.getKey() + " ");
      }
      System.out.println();
      i++;
    }

  }

  /**
  * Helper function that groups the courses at the same time slot together in
  * a Vector, so that the exam schedule can be printed.
  *
  * @pre slots is empty
  * @param g the graph
  * @param slots stores the courses at each time slot
  * @post all the courses are properly grouped by their slots
  **/
  protected static Vector<Vector<Association<String, Integer>>> allotSlots(
    Graph<Association<String, Integer>, Vector<Student>> g,
    Vector<Vector<Association<String, Integer>>> slots
  ) {

    // iterator for the vertices (courses) in g
    Iterator<Association<String, Integer>> it = g.iterator();

    while (it.hasNext()) {
      // get the course from g
      Association<String, Integer> course = it.next();
      // get the courses at its time slot
      Vector<Association<String, Integer>> s = slots.get(course.getValue());

      // if that slot does not exist
      if (s == null) {
        // create a new list of courses
        s = new Vector<>();
        s.add(course);
        slots.set(course.getValue(), s);
      } else {
        // otherwise, add the course to the existing slot
        s.add(course);
      }
    }
    return slots;
  }

  /**
   * Extra credit one, printing each course in alphabetical order
   * along with the slot it was alloted and the students who are
   * taking that course. Use the graph that was created and updated
   * to organize this information.
   *
   * @param g Graph that represents courses and students
   * @post organizes information into courses and students taking courses
   */
  public static void extraCreditOne(Graph<Association<String, Integer>, Vector<Student>> g) {

    // create a ordered vector to store a course along with all the students taking that course
    OrderedVector<ComparableAssociation<ComparableAssociation<String, Integer>, Vector<Student>>> courseAndStudents = new OrderedVector<>();

    // iterator of edges of the graph, these store students and the courses the students are taking
    Iterator<Edge<Association<String, Integer>, Vector<Student>>> edges = g.edges();

    // iterate over the egdes
    while (edges.hasNext()) {
      // the next edge
      Edge<Association<String, Integer>, Vector<Student>> sv = edges.next();

      // go over the students in edge label
      for (Student s : sv.label()) {
        // iterate over the courses of the student
        for (Association<String, Integer> c : s.getCourses()) {
          // create a course to add object to check if a certain course is already checked and added to courseAndStudents
          ComparableAssociation<ComparableAssociation<String, Integer>, Vector<Student>> courseToAdd = new ComparableAssociation<>(new ComparableAssociation<>(c.getKey(), c.getValue()));
          // if its not there we need to add to keep track of this course and the students taking it
          if (!courseAndStudents.contains(courseToAdd)) {
            // add a new course
            addCourseWithStudent(s, courseToAdd, courseAndStudents);
          } else {
            // update existing course
            addToExistingCourse(s, c, courseAndStudents);
          }
        }
      }
    }

    // print result
    printExtraCreditOne(courseAndStudents);
  }

  /**
   * Helper method for extra credit one to add to an existing course in the courseAndStudent object
   *
   * @pre nothing should be null
   * @post student is added if they are not already in
   * @param s student to add to course
   * @param c course to update
   * @param courseAndStudents data structure holding information for printing
   */
  protected static void addToExistingCourse(
    Student s,
    Association<String, Integer> c,
    OrderedVector<ComparableAssociation<ComparableAssociation<String, Integer>, Vector<Student>>> courseAndStudents
  ) {
    // this is the case that the course is already there and we just need to add students to the course
    // we also need to find it because we cannot randomly access elements in an ordered vector
    for (ComparableAssociation<ComparableAssociation<String, Integer>, Vector<Student>> course :  courseAndStudents) {
      // if it matches and the course does not already contain the student
      if (course.getKey().getKey().equals(c.getKey()) && !course.getValue().contains(s)) {
        // add the student to the list of students for that course
        course.getValue().add(s);
      }
    }
  }

  /**
   * Helper method to add new course to the courseAndStudents data structure in
   * extra credit one
   *
   * @pre nothing should be null handled in function calling
   * @post new course in data structure in extra credit one
   * @param s student taking course
   * @param course course that to add
   * @param courseAndStudents data structure to add to
   */
  protected static void addCourseWithStudent(
    Student s,
    ComparableAssociation<ComparableAssociation<String, Integer>, Vector<Student>> course,
    OrderedVector<ComparableAssociation<ComparableAssociation<String, Integer>, Vector<Student>>> courseAndStudents
  ) {
     // create a vector to hold students
     Vector<Student> students = new Vector<>();
     // add the student in question in this loop
     students.add(s);
     // set the value of the course to add as the vector of students
     course.setValue(students);
     // add this course along with the students taking it to the courseAndStudents
     courseAndStudents.add(course);
  }

  /**
   * Helper method to print out extra credit one
   *
   * @param courseAndStudents vector of course and students taking it
   */
  protected static void printExtraCreditOne(OrderedVector<ComparableAssociation<ComparableAssociation<String, Integer>, Vector<Student>>> courseAndStudents) {
    // print with all student (edge students names)
    for (ComparableAssociation<ComparableAssociation<String, Integer>, Vector<Student>> course : courseAndStudents) {
      // the key of the course has the name of the course and the slot (print that)
      System.out.print(course.getKey().getKey() + " @ slot " + (course.getKey().getValue() + 1) + " students: ");
      // now loop through the students in the course and print them
      for (Student s : course.getValue()) {
        System.out.print(s.getName() + " ");
      }
      System.out.println();
    }
  }

  /**
  * Method to generate the solution for extra credit #2. Prints out a final
  * exam schedule for each student (courses and slots), listing students in
  * alphabetical order.
  *
  * @param g the graph
  * @post the solution to extra credit #2 is printed
  **/
  public static void extraCreditTwo(Graph<Association<String, Integer>, Vector<Student>> g) {

    // an ordered vector that stores the students and their courses
    OrderedVector<ComparableAssociation<String, Vector<Association<String, Integer>>>> students = new OrderedVector<>();
    // iterator for the edges in g
    Iterator<Edge<Association<String, Integer>, Vector<Student>>> edges = g.edges();

    while (edges.hasNext()) {
      // get the edge from g
      Edge<Association<String, Integer>, Vector<Student>> sv = edges.next();
      // for each student stored in the edge (taking its two connected courses)
      for (Student s : sv.label()) {
        // get the student
        ComparableAssociation<String, Vector<Association<String, Integer>>> student = new ComparableAssociation<>(s.getName());
        if (!students.contains(student)) {
          // get all the student's courses
          Vector<Association<String, Integer>> slots = new Vector<>();
          for (Association<String, Integer> c : s.getCourses()) {
            slots.add(c);
          }
          // add the student and their courses to students
          student.setValue(slots);
          students.add(student);
        }
      }
    }

    // print the final exam schedule for each student
    for (ComparableAssociation<String, Vector<Association<String, Integer>>> s : students) {
      System.out.print(s.getKey() + ": ");

      for (Association<String, Integer> slot: s.getValue()) {
        System.out.print((slot.getValue() + 1) + " ");
      }

      System.out.println();
    }
  }

  /**
  * Main method to call the class functions. First generates the graph with
  * user inputted file, makes and prints the exam schedule, and then generates
  * the solutions for extra credit #1 and extra credit #2.
  **/
  public static void main(String[] args) {

    // scanner taking in information from command line
    Scanner scn = new Scanner(System.in);

    // graph to be edited
    Graph<Association<String, Integer>, Vector<Student>> g = new GraphListUndirected<>();

    // generate the graph
    generateGraph(g, scn);

    // make an exam schedule
    makeSchedule(g);

    // print the schedule
    printSchedule(g);
    System.out.println();

    // extra credit one
    extraCreditOne(g);
    System.out.println();

    // extra credit two
    extraCreditTwo(g);
  }
}
