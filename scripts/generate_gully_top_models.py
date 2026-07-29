"""Generate top-half gully models without rotating their textured surfaces."""

import json
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
MODEL_DIR = ROOT / "src/main/resources/assets/terranexus/models/block/drainage"
TOP_OFFSET = 13.9


def generate(source_name: str, target_name: str) -> None:
    source = MODEL_DIR / source_name
    target = MODEL_DIR / target_name
    model = json.loads(source.read_text(encoding="utf-8"))

    for element in model["elements"]:
        element["from"][1] += TOP_OFFSET
        element["to"][1] += TOP_OFFSET
        rotation = element.get("rotation")
        if rotation and "origin" in rotation:
            rotation["origin"][1] += TOP_OFFSET

    model["credit"] = f"{model.get('credit', 'Model')} | top-half translation"
    target.write_text(
        json.dumps(model, ensure_ascii=False, separators=(",", ":")),
        encoding="utf-8",
    )


generate("gully_pflaster.json", "gully_pflaster_top.json")
generate(
    "gully_kopfsteinpflaster.json",
    "gully_kopfsteinpflaster_top.json",
)
