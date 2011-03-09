/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Eclipse Public License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.eclipse.org/org/documents/epl-v10.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.ide.eclipse.adt.internal.resources.manager;

import com.android.annotations.VisibleForTesting;
import com.android.annotations.VisibleForTesting.Visibility;
import com.android.ide.eclipse.adt.internal.resources.configurations.FolderConfiguration;
import com.android.io.IAbstractFile;
import com.android.io.IAbstractFolder;
import com.android.resources.FolderTypeRelationship;
import com.android.resources.ResourceFolderType;
import com.android.resources.ResourceType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Resource Folder class. Contains list of {@link ResourceFile}s,
 * the {@link FolderConfiguration}, and a link to the {@link IAbstractFolder} object.
 */
public final class ResourceFolder implements Configurable {
    final ResourceFolderType mType;
    final FolderConfiguration mConfiguration;
    IAbstractFolder mFolder;
    ArrayList<ResourceFile> mFiles = null;
    private final ResourceRepository mRepository;


    /**
     * Creates a new {@link ResourceFolder}
     * @param type The type of the folder
     * @param config The configuration of the folder
     * @param folder The associated {@link IAbstractFolder} object.
     * @param isFrameworkRepository
     */
    protected ResourceFolder(ResourceFolderType type, FolderConfiguration config,
            IAbstractFolder folder, ResourceRepository repository) {
        mType = type;
        mConfiguration = config;
        mFolder = folder;
        mRepository = repository;
    }

    /**
     * Adds a {@link ResourceFile} to the folder.
     * @param file The {@link ResourceFile}.
     */
    @VisibleForTesting(visibility=Visibility.PROTECTED)
    public void addFile(ResourceFile file) {
        if (mFiles == null) {
            mFiles = new ArrayList<ResourceFile>();
        }

        mFiles.add(file);
    }

    protected void removeFile(ResourceFile file) {
        file.dispose();
        mFiles.remove(file);
    }

    protected void dispose() {
        for (ResourceFile file : mFiles) {
            file.dispose();
        }

        mFiles.clear();
    }

    /**
     * Returns the {@link IAbstractFolder} associated with this object.
     */
    public IAbstractFolder getFolder() {
        return mFolder;
    }

    /**
     * Returns the {@link ResourceFolderType} of this object.
     */
    public ResourceFolderType getType() {
        return mType;
    }

    public ResourceRepository getRepository() {
        return mRepository;
    }

    /**
     * Returns the list of {@link ResourceType}s generated by the files inside this folder.
     */
    public Collection<ResourceType> getResourceTypes() {
        ArrayList<ResourceType> list = new ArrayList<ResourceType>();

        if (mFiles != null) {
            for (ResourceFile file : mFiles) {
                Collection<ResourceType> types = file.getResourceTypes();

                // loop through those and add them to the main list,
                // if they are not already present
                for (ResourceType resType : types) {
                    if (list.indexOf(resType) == -1) {
                        list.add(resType);
                    }
                }
            }
        }

        return list;
    }

    public FolderConfiguration getConfiguration() {
        return mConfiguration;
    }

    /**
     * Returns whether the folder contains a file with the given name.
     * @param name the name of the file.
     */
    public boolean hasFile(String name) {
        return mFolder.hasFile(name);
    }

    /**
     * Returns the {@link ResourceFile} matching a {@link IAbstractFile} object.
     * @param file The {@link IAbstractFile} object.
     * @return the {@link ResourceFile} or null if no match was found.
     */
    public ResourceFile getFile(IAbstractFile file) {
        if (mFiles != null) {
            for (ResourceFile f : mFiles) {
                if (f.getFile().equals(file)) {
                    return f;
                }
            }
        }
        return null;
    }

    /**
     * Returns the {@link ResourceFile} matching a given name.
     * @param filename The name of the file to return.
     * @return the {@link ResourceFile} or <code>null</code> if no match was found.
     */
    public ResourceFile getFile(String filename) {
        if (mFiles != null) {
            for (ResourceFile f : mFiles) {
                if (f.getFile().getName().equals(filename)) {
                    return f;
                }
            }
        }
        return null;
    }

    /**
     * Returns whether a file in the folder is generating a resource of a specified type.
     * @param type The {@link ResourceType} being looked up.
     */
    public boolean hasResources(ResourceType type) {
        // Check if the folder type is able to generate resource of the type that was asked.
        // this is a first check to avoid going through the files.
        List<ResourceFolderType> folderTypes = FolderTypeRelationship.getRelatedFolders(type);

        boolean valid = false;
        for (ResourceFolderType rft : folderTypes) {
            if (rft == mType) {
                valid = true;
                break;
            }
        }

        if (valid) {
            if (mFiles != null) {
                for (ResourceFile f : mFiles) {
                    if (f.hasResources(type)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return mFolder.toString();
    }
}
