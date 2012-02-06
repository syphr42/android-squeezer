/*
 * Copyright (c) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.org.ngo.squeezer.model;

import java.util.Map;

import uk.org.ngo.squeezer.framework.SqueezerItem;
import android.os.Parcel;

public class SqueezerMusicFolder extends SqueezerItem {
    private String name;

    @Override
    public String getName() {
        return name;
    }

    public SqueezerMusicFolder setName(String name) {
        this.name = name;
        return this;
    }

    public SqueezerMusicFolder(String musicFolderId, String musicFolder) {
        setId(musicFolderId);
        setName(musicFolder);
    }

    public SqueezerMusicFolder(Map<String, String> record) {
        setId(record.get("id"));
        name = record.get("filename");
    }

    public static final Creator<SqueezerMusicFolder> CREATOR = new Creator<SqueezerMusicFolder>() {
        public SqueezerMusicFolder[] newArray(int size) {
            return new SqueezerMusicFolder[size];
        }

        public SqueezerMusicFolder createFromParcel(Parcel source) {
            return new SqueezerMusicFolder(source);
        }
    };

    private SqueezerMusicFolder(Parcel source) {
        setId(source.readString());
        name = source.readString();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getId());
        dest.writeString(name);
    }

    @Override
    public String toString() {
        return "id=" + getId() + ", name=" + name;
    }

}