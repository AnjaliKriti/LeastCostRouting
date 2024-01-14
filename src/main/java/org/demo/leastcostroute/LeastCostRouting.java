package org.demo.leastcostroute;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

class LeastCostRouting {
        int cost = Integer.MAX_VALUE;
        int previousCost = 0;

        List<String> sources = new ArrayList<>();
        List<String> destinations = new ArrayList<>();
        String sourceToDestinationRoute = "";
        int minCostOfNode =0;
        HashMap<String, Integer> path = new HashMap<>();
        HashMap<String, Integer> pathMapping = new HashMap<>();
        String lastNode="";
        Set<String> visitedNodes = new HashSet<>();

        private Map<String, Person> persons = new HashMap<>();
        private Map<String, NetworkElement> networkElements = new HashMap<>();
        private Map<String, List<Network>> networks = new HashMap<>();
        private Map<String, Link> links = new HashMap<>();



        public static void main(String[] args) {
            LeastCostRouting routing = new LeastCostRouting();
            routing.readDataFromCSV("Person.csv");
            System.out.println("read successfully Person");

            routing.readDataFromCSV("Network.csv");
            System.out.println("read successfully Network");

            routing.readDataFromCSV("Link.csv");
            System.out.println("read successfully Link");

            routing.readDataFromCSV("NetworkElement.csv");
            System.out.println("read successfully Network Element");

            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter Source Person: ");
            String sourcePerson = scanner.nextLine();
            System.out.print("Enter Destination Person: ");
            String destinationPerson = scanner.nextLine();

            String source = routing.getExchangeByPersonName(sourcePerson);
            String destination = routing.getExchangeByPersonName(destinationPerson);
            routing.findleastcostroute(source, destination);

        }

        private void findleastcostroute(String source, String destination) {
            ////Cost of a Route = (5X Total Processing Time) + (2 X Total Price)

            Set<String> commonExchanges = new HashSet<>(networkElements.keySet());

            // finding all sources and destination network
            for (NetworkElement nw : networkElements.values()) {
                if(nw.exchange != null && nw.exchange.equals(source)){
                    sources.add(nw.name);
                }else if(nw.exchange != null && nw.exchange.equals(destination)){
                    destinations.add(nw.name);
                }
            }

            int counter = 0;
            Map<Integer,List<String>> route = new HashMap<>();

            route.put(counter, sources);
            //  findpath()

            for(String node : sources ){
                List<Network> connectingNetwork =  networks.get(node);
                findConnectingPathFromSource(connectingNetwork, node);
                path.put(sourceToDestinationRoute,minCostOfNode);
            }
            Boolean done = false;
            while(!done) {
                String previousNode = "";
                cost = 0;
                List<String> lastNodes = new ArrayList<>();
                List<String> previousKey = new ArrayList<>();
                Map<String, Integer> modifiedPath = new HashMap<>();
                for (Map.Entry<String, Integer> p : path.entrySet()) {
                    String[] current = p.getKey().split("->");
                    findConnectingPath(current, p);
                    if (!path.containsKey(sourceToDestinationRoute)) {
                        previousKey.add(p.getKey());
                        modifiedPath.put(sourceToDestinationRoute, minCostOfNode);
                    }
                    lastNodes.add(current[current.length - 1]);
                }

                lastNodes.stream().forEach(visitedNodes::add);
                previousKey.stream().forEach(key -> path.remove(key));
                for(Map.Entry<String, Integer> m :modifiedPath.entrySet()){
                    path.put(m.getKey(),m.getValue());
                }

                List<String> pathToRemove = new ArrayList<>();
                for (Map.Entry<String, Integer> p : path.entrySet()) {
                    String node =  p.getKey().substring(p.getKey().length()-3);
                    if(visitedNodes.contains(node)) {
                        pathToRemove.add(p.getKey());
                    }
                }
                pathToRemove.stream().forEach(key -> path.remove(key));
                if(path.size() == 0) done = true;
            }

            System.out.println(pathMapping.entrySet().stream().min(Map.Entry.comparingByValue()).orElse(null));
        }


        private void findConnectingPathFromSource (List<Network> connectingNetwork , String node  ) {
            visitedNodes.add(node);
            for (Network net : connectingNetwork) {
                String nextNode = net.sourceNetworkElement.equals(node) ? net.destinationNetworkElement : net.sourceNetworkElement;
                String connectingLink = net.link;
                previousCost = cost;

                cost = Math.min(cost, 5 * (networkElements.get(node).processingTime + networkElements.get(nextNode).processingTime) + 2 * links.get(connectingLink).price);

                if (destinations.contains(nextNode)) {
                    pathMapping.put( node + "->" + nextNode, cost);
                } else {
                    if (cost < previousCost) {
                        sourceToDestinationRoute = node + "->" + nextNode;
                        minCostOfNode = cost;

                    }
                }
            }
        }

        private void findConnectingPath (String[] current ,Map.Entry<String, Integer> p  ) {

            String node = current[current.length-1];

            int sumOfProcessingTime = Arrays.stream(current)
                    .filter(networkElements::containsKey)
                    .mapToInt(key -> networkElements.get(key).processingTime)
                    .sum();

            List<Network> connectingNetwork =  networks.get(node);
            previousCost = Integer.MAX_VALUE;
            for (Network net : connectingNetwork) {
                String nextNode = net.sourceNetworkElement.equals(node) ? net.destinationNetworkElement : net.sourceNetworkElement;
                if(visitedNodes.contains(nextNode)) continue;
//                if(networkElements.containsKey(nextNode) && networkElements.get(nextNode).exchange != null
//                        && !destinations.contains(networkElements.get(nextNode).name)) continue;

                Integer totalProcessingTime = sumOfProcessingTime + networkElements.get(nextNode).processingTime;

                List<String> link = new ArrayList<>();
                for(int i =0; i< current.length ; i++){
                    String currentNode = current[i];
                    String next = i< current.length-1 ? current[i+1] : nextNode;
                    List<Network> network = networks.get(currentNode);
                    String lk = network.stream().filter(n -> (n.sourceNetworkElement.equals(currentNode) || n.destinationNetworkElement.equals(currentNode)) &&
                                    n.sourceNetworkElement.equals(next)|| n.destinationNetworkElement.equals(next))
                            .map(network1 -> network1.link).findFirst().get();
                    link.add(lk);
                }
                int totalPrice = link.stream().filter(links::containsKey).mapToInt(key -> links.get(key).price).sum();
                cost = 5* totalProcessingTime + 2* totalPrice ;

                if (destinations.contains(nextNode)) {
                    pathMapping.put( p.getKey() + "->" + nextNode ,cost);
                } else {
                    if (cost < previousCost) {
                        sourceToDestinationRoute = p.getKey() + "->" + nextNode;
                        minCostOfNode = cost;
                        previousCost = cost;

                    }
                }
            }
        }

        private String getExchangeByPersonName(String personName) {
            return persons.get(personName).exchange;
        }


        private void readDataFromCSV(String fileName) {
            String line;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(
                    Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(fileName))))) {
                br.readLine();
                while ((line = br.readLine()) != null) {
                    String[] data = line.split(",");
                    switch (fileName) {
                        case "Person.csv":
                            Person person = new Person(data[0], data[1]);
                            persons.put(person.personName, person);
                            break;
                        case "NetworkElement.csv":
                            String exchange = data.length > 2 ? data[2] : null;
                            NetworkElement ne = new NetworkElement(data[0], Integer.parseInt(data[1]), exchange);
                            networkElements.put(ne.name, ne);
                            break;
                        case "Network.csv":
                            Network network = new Network(data[0],data[1],data[2]);
                            networks.computeIfAbsent(data[0], k -> new ArrayList<>())
                                    .add(new Network(data[0], data[1], data[2]));
                            networks.computeIfAbsent(data[2], k -> new ArrayList<>())
                                    .add(new Network(data[0], data[1], data[2]));
                            break;
                        case "Link.csv":
                            Link link = new Link( data[0], Integer.parseInt(data[1]));
                            links.put(link.name, link);
                            break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

}

