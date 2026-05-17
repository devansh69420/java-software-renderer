# Java Software Renderer

A custom 3D software renderer written completely from scratch in Java.

This project started as an experiment to understand how modern graphics pipelines work internally without using OpenGL, Vulkan, DirectX, or external rendering libraries.

The renderer evolved into a procedural voxel-based rendering engine featuring custom projection, rasterization, z-buffering, lighting, quaternion camera rotation, and procedural geometry generation.

---

# Features

- Custom 3D projection pipeline
- Quaternion-based camera rotation
- Triangle rasterization
- Z-buffer depth testing
- Basic diffuse lighting
- Procedural voxel geometry generation
- Torus / donut generation using implicit equations
- Perspective projection
- Back-face culling
- BufferedImage software rendering pipeline
- Procedural mathematical surfaces

---

# Screenshots

## Procedural Voxel Torus

![Voxel Torus](images/torus.png)

---

# Technologies Used

- Java
- Java2D Graphics
- BufferedImage
- Custom math and rendering pipeline

---

# How It Works

The renderer works entirely on the CPU using software rasterization.

The general rendering pipeline is:

```text
3D Geometry
→ World Transform
→ Camera Transform
→ Perspective Projection
→ Triangle Rasterization
→ Z-Buffer Depth Test
→ Lighting
→ BufferedImage Output
→ Java2D Display
```

Voxel objects are generated procedurally using mathematical equations evaluated across 3D coordinate space.

Example torus equation:

\[
(\sqrt{x^2+y^2}-R)^2+z^2=r^2
\]

---

# Example Features

## Quaternion Camera Rotation

The camera system uses quaternion rotations to avoid gimbal lock and support free 3D movement.

## Procedural Geometry

Shapes are generated mathematically inside voxel grids using implicit equations.

Implemented examples include:
- Spheres
- Torus structures
- Twisted voxel surfaces
- Experimental procedural forms

## Lighting

Basic Lambert-style diffuse lighting is implemented using triangle normals and light direction vectors.

---

# Performance Notes

This renderer is heavily experimental and not optimized.

Current limitations:
- High memory usage for dense voxel scenes
- No greedy meshing
- No GPU acceleration
- Limited anti-aliasing
- Heavy CPU usage for large procedural scenes

Despite this, the renderer successfully handles:
- perspective-correct rendering,
- procedural voxel geometry,
- and custom software rasterization entirely on the CPU.

---

# Future Improvements

- Greedy meshing
- Face merging optimization
- Texture mapping improvements
- Multi-threaded rasterization
- Better anti-aliasing
- Frustum clipping
- Signed distance field rendering
- Marching cubes
- Real-time camera controls

---

# Why I Made This

The goal of this project was to deeply understand how 3D rendering engines actually work internally rather than relying entirely on graphics APIs or existing engines.

This project was built primarily as a learning experience in:
- computer graphics,
- linear algebra,
- rendering pipelines,
- optimization,
- and low-level graphics programming.

---

# Author

Aryan Jangid
