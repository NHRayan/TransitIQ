package com.transitiq.graph;

import com.transitiq.exception.DataCorruptionException;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GraphLoaderTest {

  @TempDir
  Path tempDir;

  @Test
  void shouldLoadValidCsv() throws Exception {
    Path nodes = tempDir.resolve("nodes.csv");
    Path edges = tempDir.resolve("edges.csv");
    Files.writeString(nodes, "id,lat,lon,type\nJ01,3.14,101.68,JUNCTION\nTS01,3.15,101.69,TRAIN_STATION");
    Files.writeString(edges, "from,to,mode,base_time_sec,max_speed_kph,district,cost,co2_per_km\nJ01,TS01,WALK,120,5,KL,0.0,0.0");

    CityGraph g = GraphLoader.loadGraph(nodes, edges);
    assertThat(g.nodeCount()).isEqualTo(2);
    assertThat(g.edgeCount()).isEqualTo(1);
  }

  @Test
  void shouldThrowOnDuplicateNode() throws Exception {
    Path nodes = tempDir.resolve("nodes.csv");
    Files.writeString(nodes, "id,lat,lon,type\nJ01,3.14,101.68,JUNCTION\nJ01,3.15,101.69,BUS_STOP");
    Path edges = tempDir.resolve("edges.csv");
    Files.writeString(edges, "from,to,mode,base_time_sec,max_speed_kph,district,cost,co2_per_km\nJ01,J01,WALK,0,5,KL,0,0");

    assertThatThrownBy(() -> GraphLoader.loadGraph(nodes, edges))
        .isInstanceOf(DataCorruptionException.class)
        .hasMessageContaining("Duplicate");
  }

  @Test
  void shouldThrowOnNegativeTime() throws Exception {
    Path nodes = tempDir.resolve("nodes.csv");
    Files.writeString(nodes, "id,lat,lon,type\nJ01,3.14,101.68,JUNCTION\nJ02,3.15,101.69,JUNCTION");
    Path edges = tempDir.resolve("edges.csv");
    Files.writeString(edges, "from,to,mode,base_time_sec,max_speed_kph,district,cost,co2_per_km\nJ01,J02,CAR,-10,40,KL,3,0.2");

    assertThatThrownBy(() -> GraphLoader.loadGraph(nodes, edges))
        .isInstanceOf(DataCorruptionException.class);
  }
}