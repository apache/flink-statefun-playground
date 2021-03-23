################################################################################
#  Licensed to the Apache Software Foundation (ASF) under one
#  or more contributor license agreements.  See the NOTICE file
#  distributed with this work for additional information
#  regarding copyright ownership.  The ASF licenses this file
#  to you under the Apache License, Version 2.0 (the
#  "License"); you may not use this file except in compliance
#  with the License.  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
# limitations under the License.
################################################################################

import json

from statefun import simple_type, make_protobuf_type

from showcase.showcase_custom_types_pb2 import UserProfile


def serialize_json_utf8(obj) -> bytes:
    """
    serialize the given object as a JSON utf-8 bytes.
    """
    str = json.dumps(obj, ensure_ascii=False)
    return str.encode('utf-8')


GREET_JSON_TYPE = simple_type(typename="showcase/GreetRequest",
                              serialize_fn=serialize_json_utf8,
                              deserialize_fn=json.loads)

USER_PROFILE_PROTOBUF_TYPE = make_protobuf_type(UserProfile)
