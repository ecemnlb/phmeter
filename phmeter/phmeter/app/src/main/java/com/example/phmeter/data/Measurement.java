
package com.example.phmeter.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Measurement {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public long timestamp;  // milliseconds since epoch
    public float pH;

    // Optional: if you save thumbnails or full images, add:
    // public String imagePath;
}
