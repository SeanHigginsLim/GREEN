# G.R.E.E.N. (Guided Realtime Exploration using Enhanced NLP)

## Names and contact details:

Proponents:
- Lim, Sean Higgins K. (sean_higgins_lim@dlsu.edu.ph / 09178949838)
- Diwajon, Hector Zachary (hector_diwajon@dlsu.edu.ph / 09285111233)
- Pajarillo, Jonah Paolo (jonah_pajarillo@dlsu.edu.ph / 09455948857)
- Jumilla, Sarah Ericka (sarah_jumilla@dlsu.edu.ph / 09561576368)

Thesis Adviser:
- Ong, Ethel (ethel.ong@dlsu.edu.ph)


## Overview of the thesis

This project implements a Virtual Tour Companion for DLSU and other potential schools who would like to adapt it. Serving as foundational platform to be extended or customized by other organizations for their own use.

G.R.E.E.N. is a virtual tour companion that creates a dynamic tour based on the user's preferences. Using the institution's information as its knoledgebase, it also serves as
a repository which users can utilize to ask queries which are relevant to them. Through the integration of Google's map navigation, it guides users to their destination.
Upon reaching certain destinations, G.R.E.E.N. provides details and information relevant to the context of the area.


## Overview of the deliverables’ file structure

The deliverables of the system are organized following the standard Android project structure. The main application code resides under the `app/src/main` directory, which contains the core components of the system.

### 📂 `app/src/main/`
This directory contains all primary source code, resources, and configuration files of the application.

- **`AndroidManifest.xml`**  
  Defines essential application configurations including permissions, activities, and system components such as the geofencing receiver.

---

### 📂 `java/com/thsst2/greenapp/`
Contains all Kotlin/Java source code organized into modular packages:

- **`algorithms/`**  
  Implements tour path generation logic including MultiGoalDijkstra, RandomBFS, DistanceCalculator, and the TourPathPlanner.

- **`data/`**  
  Contains Room database entities and repositories for local persistence.

- **`graph/`**  
  Defines the internal graph structure representing Points of Interest (POIs) and their relationships.

- **`ui.theme/`**  
  Handles application styling such as themes, colors, and typography.

- **`workers/`**  
  Manages background tasks and asynchronous processes.

- **Core Classes (Main Package)**  
  Includes key system components such as:
  - Activities (e.g., Home, Login, Map, Profile, Trivia)
  - `TourCoordinator` for orchestrating tour generation  
  - `RAGEngine` for knowledge retrieval and response generation  
  - `DialogueManager` for chatbot interaction logic  
  - `GeofenceReceiver` for location-based triggers  
  - `SessionManager` and `MetricsCollector` for tracking user sessions and performance  
  - Database-related classes (`MyAppDatabase`, DAOs, TypeConverters)

---

### 📂 `res/`
Contains all application resources used by the user interface:

- **`drawable/`** – Images and graphical assets  
- **`layout/`** – XML layout files for UI screens  
- **`mipmap-*`** – Application launcher icons for different screen densities  
- **`values/`** – Strings, colors, styles, and themes  
- **`xml/`** – Additional configuration files (e.g., map settings, permissions)

