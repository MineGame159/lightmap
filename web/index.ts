let regionsEl: HTMLElement
let positionEl: HTMLElement

let cameraX = 0
let cameraZ = 0
let cameraZoom = 1

let xMap = new Map<number, Set<number>>()

function fetchApi(route: string): Promise<Response> {
    let url = ""

    if (import.meta.env.MODE !== "production") {
        url = "http://localhost:8080"
    }

    return fetch(url + route)
}

function loadRegion(x: number, z: number) {
    let zSet = xMap.get(x)

    if (!zSet) {
        zSet = new Set<number>()
        xMap.set(x, zSet)
    }

    if (zSet.has(z)) return
    zSet.add(z)

    fetchApi(`/api/region?x=${x}&z=${z}`)
        .then(res => {
            if (!res.ok) return

            res.blob().then(blob => {
                let region = document.createElement("img")

                region.classList.add("region")
                region.src = URL.createObjectURL(blob)
                region.style.translate = `${x * 512}px ${z * 512}px`

                region.addEventListener("load", () => {
                    URL.revokeObjectURL(region.src)
                })

                regionsEl.appendChild(region)
            })
        })
}

function updateCamera() {
    // Update CSS
    let width = document.documentElement.clientWidth
    let height = document.documentElement.clientHeight

    regionsEl.style.translate = `${-(cameraX - width / 2)}px ${-(cameraZ - height / 2)}px`
    regionsEl.style.scale = (cameraZoom).toString()

    regionsEl.style.imageRendering = cameraZoom >= 1 ? "pixelated" : ""

    // Load regions
    let minX = Math.floor((cameraX - width / 2) / cameraZoom / 512)
    let minZ = Math.floor((cameraZ - height / 2) / cameraZoom / 512)

    let maxX = Math.floor((cameraX + width / 2) / cameraZoom / 512)
    let maxZ = Math.floor((cameraZ + height / 2) / cameraZoom / 512)

    for (let x = minX; x <= maxX; x++) {
        for (let z = minZ; z <= maxZ; z++) {
            loadRegion(x, z)
        }
    }
}

document.addEventListener("DOMContentLoaded", () => {
    regionsEl = document.getElementById("regions")!
    positionEl = document.getElementById("position")!
    
    cameraX = 256
    cameraZ = 256
    
    updateCamera()
})

let mouseX = 0
let mouseZ = 0
let dragging = false

document.addEventListener("mousedown", e => {
    if (e.button === 0) {
        dragging = true
    }
})

document.addEventListener("mouseup", e => {
    if (e.button === 0) {
        dragging = false
    }
})

document.addEventListener("mousemove", e => {
    mouseX = e.x
    mouseZ = e.y

    let target = getWorldMousePos()
    positionEl.textContent = `X: ${Math.floor(target.x)}, Z: ${Math.floor(target.z)}`

    if (dragging) {
        cameraX -= e.movementX
        cameraZ -= e.movementY

        updateCamera()
    }
})

document.addEventListener("wheel", e => {
    // Calculate scaled target pos before zoom
    let preTarget = getWorldMousePos()

    // Apply zoom
    cameraZoom += (e.deltaY < 0 ? 0.1 : -0.1) * cameraZoom
    cameraZoom = Math.min(Math.max(cameraZoom, 0.1), 2)

    // Calculate scaled target pos after zoom
    let postTarget = getWorldMousePos()

    // Apply offset
    cameraX -= (postTarget.x - preTarget.x) * cameraZoom
    cameraZ -= (postTarget.z - preTarget.z) * cameraZoom

    // Update camera
    updateCamera()
})

function getWorldMousePos() {
    let width = document.documentElement.clientWidth
    let height = document.documentElement.clientHeight

    let x = (cameraX - (width / 2 - mouseX)) / cameraZoom
    let z = (cameraZ - (height / 2 - mouseZ)) / cameraZoom

    return { x: x, z: z }
}
