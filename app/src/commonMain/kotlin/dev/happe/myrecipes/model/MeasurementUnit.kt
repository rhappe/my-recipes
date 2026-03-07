package dev.happe.myrecipes.model

enum class UnitFamily { Volume, Weight, Count }

enum class MeasurementUnit(val label: String, val family: UnitFamily) {
    Tsp("tsp", UnitFamily.Volume),
    Tbsp("tbsp", UnitFamily.Volume),
    Cup("cup", UnitFamily.Volume),
    FlOz("fl oz", UnitFamily.Volume),
    Ml("ml", UnitFamily.Volume),
    Liter("l", UnitFamily.Volume),
    Oz("oz", UnitFamily.Weight),
    Lb("lb", UnitFamily.Weight),
    Gram("g", UnitFamily.Weight),
    Kg("kg", UnitFamily.Weight),
    Pinch("pinch", UnitFamily.Count),
    Clove("clove", UnitFamily.Count),
    Whole("whole", UnitFamily.Count),
    Slice("slice", UnitFamily.Count),
    Can("can", UnitFamily.Count),
    Package("pkg", UnitFamily.Count),
}
