package org.example.questionBankApplication.model;

import java.util.Map;
import java.util.Set;

public class SubjectTopicMap {

    public static Set<Topic> getTopics(Subject subject) {
        if (subject == null) {
            throw new IllegalStateException("Supplied Argument to method is null");
        }
        return map.get(subject);
    }

    public static Subject getSubject(Topic topic) {

        if (topic == null) {
            throw new IllegalStateException("Supplied Argument to method is null");
        }

        for (Map.Entry<Subject, Set<Topic>> entry : map.entrySet()) {
            if (entry.getValue().contains(topic)) {
                return entry.getKey();
            }
        }

        throw new IllegalStateException("Topic not found");
    }

    public static boolean isValidTopic(Subject subject, Topic topic) {

        if (subject == null || topic == null) {
            throw new IllegalStateException("Supplied Argument to method is null");
        }

        return map.get(subject).contains(topic);
    }

    public static Set<Subject> getAllSubjects() {
        return map.keySet();
    }

    public static Set<Topic> getAllTopics() {
        Set<Topic> topics = new java.util.HashSet<>();

        for (Set<Topic> subjectTopics : map.values()) {
            topics.addAll(subjectTopics);
        }
        // return a Unmodifiable set
        return Set.copyOf(topics);
    }



    private static final Map<Subject, Set<Topic>> map = createMap();

    private static Map<Subject, Set<Topic>> createMap() {
        return Map.ofEntries(
                Map.entry(Subject.Discrete_Mathematics,
                        Set.of(
                                Topic.Propositional_Logic,
                                Topic.Set_Theory,
                                Topic.Relation,
                                Topic.Functions,
                                Topic.Lattice,
                                Topic.Group_Theory,
                                Topic.Graph_Theory,
                                Topic.Planer_Graph,
                                Topic.Combination,
                                Topic.Probability_Theory,
                                Topic.Recurrence
                        )
                ),

                Map.entry(Subject.Theory_of_Computation,
                        Set.of(
                                Topic.Regular_Expression,
                                Topic.Regular_Grammar,
                                Topic.Regular_Language,
                                Topic.Finite_Automata,
                                Topic.Context_Free_Grammar,
                                Topic.Context_Free_Language,
                                Topic.Pushdown_Automata,
                                Topic.Recursive_Language,
                                Topic.Turing_Machine,
                                Topic.Undecidability
                        )
                ),

                Map.entry(Subject.Digital_Logic,
                        Set.of(
                                Topic.Number_System,
                                Topic.Boolean_Algebra,
                                Topic.Combinational_Circuit,
                                Topic.Sequential_Circuit
                        )
                ),

                Map.entry(Subject.Computer_Organisation_and_Architecture,
                        Set.of(
                                Topic.Machine_Instruction,
                                Topic.Addressing_Modes,
                                Topic.ALU_Data_Path_and_Control_Unit,
                                Topic.IO_Interface,
                                Topic.Interrupt,
                                Topic.Pipeline_Processor,
                                Topic.Cache_Memory,
                                Topic.Secondary_Storage,
                                Topic.Memory_Chip_Design
                        )
                ),

                Map.entry(Subject.Data_Structure,
                        Set.of(
                                Topic.Array,
                                Topic.linked_List,
                                Topic.Stack,
                                Topic.Queue,
                                Topic.Binary_Tree,
                                Topic.Binary_Search_Tree,
                                Topic.AVL_Tree,
                                Topic.B_Tree,
                                Topic.B_Plus_Tree,
                                Topic.Heap_Tree,
                                Topic.n_ary_Tree,
                                Topic.Hashing
                        )
                ),

                Map.entry(Subject.Algorithms,
                        Set.of(
                                Topic.Asymptotic_Notation,
                                Topic.Recurrence_Relation,
                                Topic.Divide_and_Conquer,
                                Topic.Sorting,
                                Topic.Greedy_Technique,
                                Topic.Minimum_Spanning_Tree,
                                Topic.Shortest_Path,
                                Topic.Graph_Traversal,
                                Topic.Dynamic_Programming
                        )
                ),

                Map.entry(Subject.C_Programming,
                        Set.of(
                                Topic.C_Programming
                        )
                ),

                Map.entry(Subject.Operating_System,
                        Set.of(
                                Topic.Process,
                                Topic.CPU_Scheduling,
                                Topic.Process_Synchronisation,
                                Topic.Deadlock,
                                Topic.Memory_Management,
                                Topic.File_Systems,
                                Topic.Disk_Scheduling,
                                Topic.System_Call,
                                Topic.Thread
                        )
                ),

                Map.entry(Subject.Computer_Networks,
                        Set.of(
                                Topic.OSI_Layer,
                                Topic.Physical_Layer,
                                Topic.Data_Link_Layer,
                                Topic.Network_Layer_Protocol,
                                Topic.Transport_Layer_protocol,
                                Topic.Application_Layer_protocol
                        )
                ),

                Map.entry(Subject.Databases,
                        Set.of(
                                Topic.ER_Model,
                                Topic.Relational_Schema,
                                Topic.Relational_Algebra,
                                Topic.Normal_Form,
                                Topic.Transactions,
                                Topic.Integrity_Constraints,
                                Topic.SQL,
                                Topic.Tuple_Calculus,
                                Topic.File_System
                        )
                ),

                Map.entry(Subject.Compiler_Design,
                        Set.of(
                                Topic.Lexical_Analysis,
                                Topic.Parsing,
                                Topic.Syntax_Directed_Translation,
                                Topic.Intermediate_Code_Generation,
                                Topic.Runtime_Environment,
                                Topic.Matching
                        )
                ),

                Map.entry(Subject.Engineering_Mathematics,
                        Set.of(
                                Topic.Linear_Algebra,
                                Topic.Calculus,
                                Topic.Numerical_Methods
                        )
                ),

                Map.entry(Subject.General_Aptitude,
                        Set.of(
                                Topic.General_Aptitude
                        )
                )

        );
    }


    // Validation of the Map
    static {
        // Validate the number of subjects
        if (map.size() != 13) {
            throw new IllegalStateException("Expected 13 subjects, but found " + map.size());
        }

        if (map.size() != Subject.values().length) {
            throw new IllegalStateException("Mismatch between number of Subjects in enum and keys stored in Map");
        }

        // Validate the number of topics under each subject
        checkTopicCount(Subject.Discrete_Mathematics, 11);
        checkTopicCount(Subject.Theory_of_Computation, 10);
        checkTopicCount(Subject.Digital_Logic, 4);
        checkTopicCount(Subject.Computer_Organisation_and_Architecture, 9);
        checkTopicCount(Subject.Data_Structure, 12);
        checkTopicCount(Subject.Algorithms, 9);
        checkTopicCount(Subject.C_Programming, 1);
        checkTopicCount(Subject.Operating_System, 9);
        checkTopicCount(Subject.Computer_Networks, 6);
        checkTopicCount(Subject.Databases, 9);
        checkTopicCount(Subject.Compiler_Design, 6);
        checkTopicCount(Subject.Engineering_Mathematics, 3);
        checkTopicCount(Subject.General_Aptitude, 1);

        // Validate the total number of topic entries
        checkTotalTopicCount();

        // Validate if one topic is associated with one and only one subject.
        checkTopicMappings();
    }

    private static void checkTopicCount(Subject subject, int expectedCount) {
        Set<Topic> topics = map.get(subject);
        if (topics == null || topics.size() != expectedCount) {
            throw new IllegalStateException(
                            "Subject " + subject
                            + " should have " + expectedCount
                            + " topics, but found "
                            + (topics == null ? 0 : topics.size())
            );
        }
    }

    private static void checkTotalTopicCount() {

        int totalTopicsInMap = map.values()
                .stream()
                .mapToInt(Set::size)
                .sum();

        int expectedCount = Topic.values().length;

        if (totalTopicsInMap != expectedCount) {
            throw new IllegalStateException("Mismatch between number of Topics in enum and values stored in Map");
        }
    }

    private static void checkTopicMappings() {

        for (Topic topic : Topic.values()) {

            int count = 0;

            for (Set<Topic> topics : map.values()) {
                if (topics.contains(topic)) {
                    count++;
                }
            }

            if (count == 0) {
                throw new IllegalStateException("Topic " + topic + " is not assigned to any subject");
            }

            if (count > 1) {
                throw new IllegalStateException("Topic " + topic + " is assigned to multiple subjects");
            }
        }
    }

}
