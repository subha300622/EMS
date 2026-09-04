package com.example.ems.payroll.service;

import com.example.ems.payroll.entity.CalculationBaseType;
import com.example.ems.payroll.entity.CalculationType;
import com.example.ems.payroll.entity.SalaryStructureComponent;
import com.example.ems.payroll.validation.SalaryDependencyNode;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SalaryDependencyGraphService {

    public Map<Long, SalaryDependencyNode> buildGraph(List<SalaryStructureComponent> components) {
        Map<Long, SalaryDependencyNode> nodeMap = new LinkedHashMap<>();

        // 1. Initialize nodes
        for (SalaryStructureComponent ssc : components) {
            Long compId = ssc.getSalaryComponent().getId();
            String compCode = ssc.getSalaryComponent().getCode();
            String compName = ssc.getSalaryComponent().getName();
            nodeMap.put(compId, new SalaryDependencyNode(compId, compCode, compName));
        }

        // 2. Populate dependencies
        for (SalaryStructureComponent ssc : components) {
            Long compId = ssc.getSalaryComponent().getId();
            SalaryDependencyNode node = nodeMap.get(compId);

            if (ssc.getCalculationType() == CalculationType.PERCENTAGE
                    && ssc.getCalculationBaseType() == CalculationBaseType.COMPONENT
                    && ssc.getCalculationBaseComponent() != null) {
                Long baseId = ssc.getCalculationBaseComponent().getId();
                node.getDependencies().add(baseId);

                SalaryDependencyNode baseNode = nodeMap.get(baseId);
                if (baseNode != null) {
                    baseNode.getDependents().add(compId);
                }
            } else if (ssc.getCalculationType() == CalculationType.PERCENTAGE
                    && ssc.getCalculationBaseType() == CalculationBaseType.GROSS) {
                for (SalaryStructureComponent other : components) {
                    if (other.getSalaryComponent().getComponentType() == com.example.ems.payroll.entity.SalaryComponentType.EARNING
                            && !other.getSalaryComponent().getId().equals(compId)) {
                        Long earnId = other.getSalaryComponent().getId();
                        node.getDependencies().add(earnId);

                        SalaryDependencyNode earnNode = nodeMap.get(earnId);
                        if (earnNode != null) {
                            earnNode.getDependents().add(compId);
                        }
                    }
                }
            } else if (ssc.getCalculationType() == CalculationType.FORMULA && ssc.getFormula() != null) {
                for (SalaryStructureComponent other : components) {
                    if (!other.getSalaryComponent().getId().equals(compId)
                            && ssc.getFormula().contains(other.getSalaryComponent().getCode())) {
                        Long otherId = other.getSalaryComponent().getId();
                        node.getDependencies().add(otherId);

                        SalaryDependencyNode otherNode = nodeMap.get(otherId);
                        if (otherNode != null) {
                            otherNode.getDependents().add(compId);
                        }
                    }
                }
            }
        }

        return nodeMap;
    }

    /**
     * Detects circular dependencies using DFS graph coloring:
     * WHITE (0 = Unvisited), GRAY (1 = Visiting / in current recursion stack), BLACK (2 = Visited / Completed)
     *
     * @return Optional containing cycle path description if a cycle exists (e.g. "A → B → C → A"), or Optional.empty()
     */
    public Optional<String> detectCycle(Map<Long, SalaryDependencyNode> nodes) {
        Map<Long, Integer> state = new HashMap<>(); // 0: unvisited, 1: visiting, 2: visited
        List<Long> path = new ArrayList<>();

        for (Long nodeId : nodes.keySet()) {
            state.put(nodeId, 0);
        }

        for (Long startNodeId : nodes.keySet()) {
            if (state.get(startNodeId) == 0) {
                Optional<String> cycle = dfsCycle(startNodeId, nodes, state, path);
                if (cycle.isPresent()) {
                    return cycle;
                }
            }
        }
        return Optional.empty();
    }

    private Optional<String> dfsCycle(Long currentId, Map<Long, SalaryDependencyNode> nodes, Map<Long, Integer> state, List<Long> path) {
        state.put(currentId, 1); // Mark as visiting (in recursion stack)
        path.add(currentId);

        SalaryDependencyNode currentNode = nodes.get(currentId);
        if (currentNode != null) {
            for (Long dependencyId : currentNode.getDependencies()) {
                // If the dependency exists in our graph
                if (nodes.containsKey(dependencyId)) {
                    Integer depState = state.get(dependencyId);
                    if (depState == 1) {
                        // Found a back-edge in the DFS tree -> Cycle!
                        int cycleStartIndex = path.indexOf(dependencyId);
                        List<Long> cycleIds = new ArrayList<>(path.subList(cycleStartIndex, path.size()));
                        cycleIds.add(dependencyId);

                        String cyclePathStr = cycleIds.stream()
                                .map(id -> nodes.get(id) != null ? nodes.get(id).getComponentCode() : "ID_" + id)
                                .collect(Collectors.joining(" → "));

                        return Optional.of(cyclePathStr);
                    } else if (depState == 0) {
                        Optional<String> cycle = dfsCycle(dependencyId, nodes, state, path);
                        if (cycle.isPresent()) {
                            return cycle;
                        }
                    }
                }
            }
        }

        path.remove(path.size() - 1);
        state.put(currentId, 2); // Mark as completed
        return Optional.empty();
    }

    /**
     * Performs a deterministic topological sort using Kahn's algorithm.
     * Nodes with 0 in-degree (no unresolved dependencies) are processed first.
     *
     * @return List of component IDs in calculation execution order
     */
    public List<Long> topologicalSort(Map<Long, SalaryDependencyNode> nodes) {
        Map<Long, Integer> inDegree = new HashMap<>();
        for (Map.Entry<Long, SalaryDependencyNode> entry : nodes.entrySet()) {
            // In-degree is the count of valid dependencies inside this graph
            long validDeps = entry.getValue().getDependencies().stream()
                    .filter(nodes::containsKey)
                    .count();
            inDegree.put(entry.getKey(), (int) validDeps);
        }

        // Maintain deterministic order: components with 0 in-degree
        Queue<Long> queue = new LinkedList<>();
        for (Long nodeId : nodes.keySet()) {
            if (inDegree.get(nodeId) == 0) {
                queue.add(nodeId);
            }
        }

        List<Long> result = new ArrayList<>();

        while (!queue.isEmpty()) {
            Long u = queue.poll();
            result.add(u);

            SalaryDependencyNode node = nodes.get(u);
            if (node != null) {
                for (Long v : node.getDependents()) {
                    if (inDegree.containsKey(v)) {
                        inDegree.put(v, inDegree.get(v) - 1);
                        if (inDegree.get(v) == 0) {
                            queue.add(v);
                        }
                    }
                }
            }
        }

        return result;
    }
}
