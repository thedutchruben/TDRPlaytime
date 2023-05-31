package nl.thedutchruben.playtime.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TopPlaceData {
    private String name;
    private String uuid;
    private long time;
}