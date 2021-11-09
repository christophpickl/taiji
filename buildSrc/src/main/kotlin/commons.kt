fun log(msg: () -> String) {
    if (Constants.LOG_ENABLED) {
        println("[LOG] ${msg()}")
    }
}
