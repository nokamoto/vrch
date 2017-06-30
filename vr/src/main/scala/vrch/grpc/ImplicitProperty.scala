package vrch.grpc

object ImplicitProperty {
  implicit class Property(key: String) {
    def stringProp: String = sys.props.getOrElse(key, throw UninitializedFieldError(s"$key undefined"))

    def intProp: Int = stringProp.toInt
  }
}
