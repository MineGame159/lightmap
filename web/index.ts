let regionsEl: HTMLElement
let claimsEl: HTMLElement

let claimEl: HTMLElement
let positionEl: HTMLElement

let cameraX = 0
let cameraZ = 0
let cameraZoom = 1

let xMap = new Map<number, Set<number>>()

let claims: Claim[]

interface Claim {
    name: string
    color: Color
    chunks: Chunk[]

    x: number
    z: number
    width: number
    height: number
}

interface Color {
    r: number
    g: number
    b: number
}

interface Chunk {
    x: number
    z: number
}

function isOverClaim(claim: Claim, pos: { x: number, z: number }): boolean {
    if (pos.x < claim.x || pos.x >= claim.x + claim.width || pos.z < claim.z || pos.z >= claim.z + claim.height) {
        return false
    }

    for (const chunk of claim.chunks) {
        if (pos.x >= chunk.x * 16 && pos.x < (chunk.x + 1) * 16 && pos.z >= chunk.z * 16 && pos.z < (chunk.z + 1) * 16) {
            return true
        }
    }

    return false
}

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

    fetchApi(`/api/overworld/region?x=${x}&z=${z}`)
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

function loadClaims() {
    fetchApi("/api/overworld/claims")
        .then(res => res.json())
        .then(claimsO => {
            for (const claim of (claimsO as Claim[])) {
                let svg = document.createElementNS("http://www.w3.org/2000/svg", "svg")

                let minX = Number.MAX_SAFE_INTEGER
                let minZ = Number.MAX_SAFE_INTEGER

                let maxX = Number.MIN_SAFE_INTEGER
                let maxZ = Number.MIN_SAFE_INTEGER

                claim.chunks.forEach(chunk => {
                    minX = Math.min(minX, chunk.x)
                    minZ = Math.min(minZ, chunk.z)

                    maxX = Math.max(maxX, chunk.x)
                    maxZ = Math.max(maxZ, chunk.z)
                })

                let width = (Math.abs(maxX - minX) + 1) * 16
                let height = (Math.abs(maxZ - minZ) + 1) * 16

                svg.setAttribute("width", width + "px")
                svg.setAttribute("height", height + "px")
                svg.setAttribute("viewBox", `0 0 ${width} ${height}`)
                svg.setAttribute("fill", `rgba(${claim.color.r}, ${claim.color.g}, ${claim.color.b}, 0.5)`)
                svg.setAttribute("stroke", "none")

                svg.style.translate = `${minX * 16}px ${minZ * 16}px`

                claim.chunks.forEach(chunk => {
                    let rect = document.createElementNS("http://www.w3.org/2000/svg", "rect")

                    rect.setAttribute("x", ((chunk.x - minX) * 16) + "px")
                    rect.setAttribute("y", ((chunk.z - minZ) * 16) + "px")
                    rect.setAttribute("width", "16px")
                    rect.setAttribute("height", "16px")

                    svg.appendChild(rect)
                })

                claimsEl.appendChild(svg)

                claim.x = minX * 16
                claim.z = minZ * 16
                claim.width = width * 16
                claim.height = height * 16
            }

            claims = claimsO as Claim[]
        })
}

function updateCamera() {
    // Update CSS
    let width = document.documentElement.clientWidth
    let height = document.documentElement.clientHeight

    regionsEl.style.translate = `${-(cameraX - width / 2)}px ${-(cameraZ - height / 2)}px`
    regionsEl.style.scale = (cameraZoom).toString()

    claimsEl.style.translate = `${-(cameraX - width / 2)}px ${-(cameraZ - height / 2)}px`
    claimsEl.style.scale = (cameraZoom).toString()

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
    claimsEl = document.getElementById("claims")!

    claimEl = document.getElementById("claim")!
    positionEl = document.getElementById("position")!
    
    loadClaims()

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

    let hoveredClaim: Claim

    for (const claim of claims) {
        if (isOverClaim(claim, target)) {
            hoveredClaim = claim
            break
        }
    }

    if (hoveredClaim) claimEl.textContent = hoveredClaim.name
    else claimEl.textContent = ""

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
