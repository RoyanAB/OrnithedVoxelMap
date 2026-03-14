package com.mamiyaotaru.voxelmap.util;

import com.mamiyaotaru.voxelmap.interfaces.IDimensionManager;
import com.mamiyaotaru.voxelmap.interfaces.IVoxelMap;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class DimensionManager implements IDimensionManager {
    public static final int NOT_LOADED_ID = -9999;
    public static final String NOT_LOADED = "Unknown Dimension";
    public static final String FAILED_TO_LOAD = "Failed Dimension";
    public ArrayList<DimensionContainer> dimensions;
    IVoxelMap master;

    public DimensionManager(IVoxelMap master) {
        this.master = master;
        this.dimensions = new ArrayList<>();
    }

    @Override
    public ArrayList<DimensionContainer> getDimensions() {
        return this.dimensions;
    }

    @Override
    public void populateDimensions() {
        this.dimensions.clear();

        for (DimensionType dimensionType : Registry.DIMENSION) {
            int id = dimensionType.getRawId();
            String name = DimensionType.getId(dimensionType).getPath();
            Identifier resourceLocation = DimensionType.getId(dimensionType);
            DimensionContainer dim = new DimensionContainer(dimensionType, name, id, resourceLocation);
            this.dimensions.add(dim);
        }

        this.sort();
    }

    @Override
    public void enteredDimension(Dimension dimension) {
        int id = dimension.getType().getRawId();
        DimensionContainer dim = this.getDimensionContainerByID(id);
        if (dim == null) {
            dim = this.getDimensionContainerByResourceLocation(DimensionType.getId(dimension.getType()));
        }

        if (dim == null) {
            dim = new DimensionContainer(dimension.getType(), "Unknown Dimension", id, null);
            this.dimensions.add(dim);
            this.sort();
        }

        if (dim.name.equals("Unknown Dimension") || dim.name.equals("Failed Dimension")) {
            DimensionType type = dimension.getType();
            dim.type = type;
            dim.id = id;

            try {
                dim.name = DimensionType.getId(type).getPath();
                dim.resourceLocation = DimensionType.getId(type);
            } catch (Exception var6) {
            }
        }
    }

    private void sort() {
        Collections.sort(this.dimensions, new Comparator<DimensionContainer>() {
            public int compare(DimensionContainer dim1, DimensionContainer dim2) {
                return dim1.id - dim2.id;
            }
        });
    }

    @Override
    public DimensionContainer getDimensionContainerByDimension(Dimension dimension) {
        int id = dimension.getType().getRawId();
        DimensionContainer dim = this.getDimensionContainerByID(id);
        if (dim == null) {
            DimensionType type = dimension.getType();
            Identifier resourceLocation = DimensionType.getId(type);
            dim = new DimensionContainer(dimension.getType(), DimensionType.getId(type).getPath(), id, resourceLocation);
            this.dimensions.add(dim);
            this.sort();
        }

        return dim;
    }

    @Override
    public DimensionContainer getDimensionContainerByIdentifier(String ident) {
        DimensionContainer dim = null;
        int id = -9999;
        Identifier resourceLocation = null;

        try {
            id = Integer.parseInt(ident);
        } catch (NumberFormatException var8) {
        }

        if (id != -9999) {
            dim = this.getDimensionContainerByID(id);
        } else if (ident.contains(" ")) {
            String[] parts = ident.split(" ");
            resourceLocation = new Identifier(parts[0]);

            try {
                id = Integer.parseInt(parts[1]);
            } catch (NumberFormatException var7) {
            }

            if (id != -9999) {
                dim = this.getDimensionContainerByID(id);
            } else {
                dim = this.getDimensionContainerByResourceLocation(resourceLocation);
            }
        } else {
            resourceLocation = new Identifier(ident);
            dim = this.getDimensionContainerByResourceLocation(new Identifier(ident));
        }

        if (dim == null) {
            dim = new DimensionContainer(null, "Unknown Dimension", id, resourceLocation);
            this.dimensions.add(dim);
            this.sort();
        }

        return dim;
    }

    private DimensionContainer getDimensionContainerByID(int id) {
        for (DimensionContainer dim : this.dimensions) {
            if (dim.id == id) {
                return dim;
            }
        }

        return null;
    }

    private DimensionContainer getDimensionContainerByResourceLocation(Identifier resourceLocation) {
        for (DimensionContainer dim : this.dimensions) {
            if (resourceLocation.equals(dim.resourceLocation)) {
                return dim;
            }
        }

        return null;
    }
}
