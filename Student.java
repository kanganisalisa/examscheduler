// We are the sole authors of the work in this repository.

import structure5.*;

/**
 * Class to store students and the courses they are taking
 * These will be represented in the label of the graph
 */
public class Student {

  // name of student
  protected String name;
  // courses the student is taking
  protected Vector<Association<String, Integer>> courses;

  /**
   * Contructor of student
   * @param name string their name
   */
  public Student(String studentName) {
    // set name
    this.name = studentName;
    // initizalize vector of courses
    courses = new Vector<>();
  }

  /**
   * Add a course to the students courses
   * @param c course to add
   */
  public void addCourse(Association<String, Integer> c) {

    // if the student is already taking that course dont do anything
    if (courses.contains(c)) {
      return;
    }
    // else add it
    courses.add(c);
  }

  /**
   * Add many courses at once
   * @param c
   */
  public void addCourses(Vector<Association<String, Integer>> c) {
    // go thru and call add course on each
    for (Association<String, Integer> course : c) {
      addCourse(course);
    }
  }

  /**
   * Get the course the student is taking
   * @return vector of courses
   */
  public Vector<Association<String, Integer>> getCourses() {
    return courses;
  }

  /**
   * Get name of student
   * @return string name
   */
  public String getName() {
    return name;
  }

  /**
   * Equals method
   * @param other student to compare to
   * @return true iff they are equal
   */
  public boolean equals(Student other) {
    return compareTo(other) == 0;
  }

  /**
   * Compare to method
   * @param other student to compare to
   * @return int 0 if equal, neg if other is greater, postive if this is greater
   */
  public int compareTo(Student other) {
    return name.compareTo(other.getName());
  }


}
