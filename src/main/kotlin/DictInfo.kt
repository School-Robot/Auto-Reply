package tk.mcsog

import kotlinx.serialization.Serializable

@Serializable
data class DictInfo (
    val dictName: String,
    var dictList: HashMap<String, String> = hashMapOf<String, String>("test" to "test")
)