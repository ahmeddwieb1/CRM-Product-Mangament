db.meetings.aggregate([
    {
        $lookup:
            {
                from: "users",
                let: {cid: "$assignedToId"},
                pipeline: [
                    {$match: {$expr: {$eq: ["$_id", "$$cid"]}}},
                    {$project: {username: 1, _id: 0}}
                ],
                as: "user"
            }
    },
    {
        $lookup:
            {
                from: "lead",
                let: {lid: "$clientId"},
                pipeline: [
                    {$match: {$expr: {$eq: ["$_id", "$$lid"]}}},
                    {$project: {leadName: 1, _id: 0}}
                ],
                as: "lead"
            }
    }
])